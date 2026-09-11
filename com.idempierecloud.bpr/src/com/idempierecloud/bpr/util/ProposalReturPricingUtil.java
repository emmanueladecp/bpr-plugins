package com.idempierecloud.bpr.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MOrder;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPricing;
import org.compiere.model.MUOMConversion;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * Shared pricing logic khusus Proposal Retur.
 *
 * Dipakai oleh:
 *
 * 1. SetProposalReturPricing.java
 *    -> Callout / UI
 *
 * 2. COrderLineEvent.java
 *    -> API / Process / backend
 *
 *
 * Business Rule:
 *
 * PriceList:
 *   - Prioritas dari Last AR Invoice
 *   - Kalau tidak ada -> Price List Order yang berlaku
 *
 * subsidiAmt:
 *   - Last Invoice
 *   - Kalau tidak ada -> 0
 *
 * ongkosAngkut:
 *   - Last Invoice
 *   - Kalau tidak ada -> 0
 *
 * PriceActual:
 *
 *   PriceList + subsidiAmt + ongkosAngkut
 *
 * PriceActual selalu merupakan harga dalam
 * BASE UOM Product.
 *
 *
 * PriceEntered:
 *
 *   Harga PriceActual yang dikonversi
 *   ke C_UOM_ID yang dipilih pada Order Line.
 *
 *
 * LineNetAmt:
 *
 *   PriceEntered * QtyEntered
 *
 */
public final class ProposalReturPricingUtil {

    public static final String COLUMN_SUBSIDI_AMT =
            "SubsidiAmt";

    public static final String COLUMN_ONGKOS_ANGKUT =
            "OngkosAngkut";


    /**
     * Precision internal untuk konversi PRICE.
     *
     * Core MOrderLine sendiri menggunakan precision cukup tinggi
     * ketika menghitung PriceEntered dari ratio QtyOrdered /
     * QtyEntered.
     */
    private static final int PRICE_CONVERSION_PRECISION = 12;


    private ProposalReturPricingUtil() {
        // Utility class
    }


    // ============================================================
    // INITIAL PRICING
    // ============================================================

    /**
     * Digunakan ketika Product pertama kali dipilih /
     * C_OrderLine dibuat.
     *
     * Priority:
     *
     * 1. Last AR Invoice
     * 2. Current Order Price List
     *
     *
     * @param order
     * @param M_Product_ID
     * @param C_UOM_ID UOM yang dipilih pada OrderLine
     * @param qtyEntered
     * @param qtyOrdered quantity dalam Base UOM; digunakan
     *                   oleh pricing engine untuk price break
     * @param trxName
     *
     * @return pricing result lengkap
     */
    public static PricingResult resolveInitialPricing(
            MOrder order,
            int M_Product_ID,
            int C_UOM_ID,
            BigDecimal qtyEntered,
            BigDecimal qtyOrdered,
            String trxName) {

        if (order == null) {
            throw new AdempiereException(
                    "Order tidak ditemukan."
            );
        }

        if (M_Product_ID <= 0) {
            throw new AdempiereException(
                    "Product belum dipilih."
            );
        }


        MProduct product =
                new MProduct(
                        order.getCtx(),
                        M_Product_ID,
                        trxName
                );


        if (product.get_ID() <= 0) {
            throw new AdempiereException(
                    "Product ID "
                    + M_Product_ID
                    + " tidak ditemukan."
            );
        }


        /*
         * Kalau C_UOM_ID belum ada,
         * fallback ke Base UOM Product.
         */
        int effectiveUOM_ID =
                C_UOM_ID > 0
                        ? C_UOM_ID
                        : product.getC_UOM_ID();


        // ========================================================
        // SOURCE PRICE
        // ========================================================

        LastInvoicePricing lastInvoice =
                getLastInvoicePricing(
                        order,
                        M_Product_ID,
                        trxName
                );


        BigDecimal priceList;
        BigDecimal subsidiAmt;
        BigDecimal ongkosAngkut;

        boolean fromLastInvoice;
        int C_Invoice_ID;


        if (lastInvoice != null) {

            // ====================================================
            // LAST INVOICE FOUND
            // ====================================================

            priceList =
                    nvl(
                            lastInvoice.getPriceList()
                    );

            subsidiAmt =
                    nvl(
                            lastInvoice.getSubsidiAmt()
                    );

            ongkosAngkut =
                    nvl(
                            lastInvoice.getOngkosAngkut()
                    );

            fromLastInvoice =
                    true;

            C_Invoice_ID =
                    lastInvoice.getC_Invoice_ID();

        } else {

            // ====================================================
            // FALLBACK CURRENT PRICE LIST
            // ====================================================

            priceList =
                    getCurrentPriceList(
                            order,
                            M_Product_ID,
                            qtyOrdered,
                            trxName
                    );


            if (priceList == null) {

                throw new AdempiereException(
                        "Harga Product '"
                        + product.getName()
                        + "' tidak ditemukan pada "
                        + "Price List yang berlaku."
                );
            }


            subsidiAmt =
                    Env.ZERO;

            ongkosAngkut =
                    Env.ZERO;

            fromLastInvoice =
                    false;

            C_Invoice_ID =
                    0;
        }


        /*
         * Setelah source ditemukan,
         * calculate seluruh derived field.
         */
        PricingResult result =
                calculateDerivedPricing(
                        order,
                        M_Product_ID,
                        effectiveUOM_ID,
                        qtyEntered,
                        priceList,
                        subsidiAmt,
                        ongkosAngkut,
                        trxName
                );


        result.setFromLastInvoice(
                fromLastInvoice
        );

        result.setC_Invoice_ID(
                C_Invoice_ID
        );


        return result;
    }


    // ============================================================
    // DERIVED PRICING
    // ============================================================

    /**
     * Digunakan ketika source pricing sudah diketahui.
     *
     * Contoh:
     *
     * PriceList berubah
     * subsidiAmt berubah
     * ongkosAngkut berubah
     * C_UOM_ID berubah
     * QtyEntered berubah
     *
     * Tidak query Last Invoice lagi.
     */
    public static PricingResult calculateDerivedPricing(
            MOrder order,
            int M_Product_ID,
            int C_UOM_ID,
            BigDecimal qtyEntered,
            BigDecimal priceList,
            BigDecimal subsidiAmt,
            BigDecimal ongkosAngkut,
            String trxName) {

        if (order == null) {
            throw new AdempiereException(
                    "Order tidak ditemukan."
            );
        }


        if (M_Product_ID <= 0) {
            throw new AdempiereException(
                    "Product belum dipilih."
            );
        }


        MProduct product =
                new MProduct(
                        order.getCtx(),
                        M_Product_ID,
                        trxName
                );


        if (product.get_ID() <= 0) {

            throw new AdempiereException(
                    "Product ID "
                    + M_Product_ID
                    + " tidak ditemukan."
            );
        }


        int effectiveUOM_ID =
                C_UOM_ID > 0
                        ? C_UOM_ID
                        : product.getC_UOM_ID();


        priceList =
                nvl(priceList);

        subsidiAmt =
                nvl(subsidiAmt);

        ongkosAngkut =
                nvl(ongkosAngkut);

        qtyEntered =
                nvl(qtyEntered);


        // ========================================================
        // PRICE ACTUAL
        // ========================================================

        BigDecimal priceActual =
                calculatePriceActual(
                        priceList,
                        subsidiAmt,
                        ongkosAngkut
                );


        // ========================================================
        // PRICE ENTERED
        // ========================================================

        BigDecimal priceEntered =
                calculatePriceEntered(
                        product,
                        effectiveUOM_ID,
                        priceActual
                );


        // ========================================================
        // LINE NET AMT
        // ========================================================

        BigDecimal lineNetAmt =
                calculateLineNetAmt(
                        order,
                        priceEntered,
                        qtyEntered
                );


        PricingResult result =
                new PricingResult();


        result.setM_Product_ID(
                M_Product_ID
        );

        result.setC_UOM_ID(
                effectiveUOM_ID
        );

        result.setPriceList(
                priceList
        );

        result.setSubsidiAmt(
                subsidiAmt
        );

        result.setOngkosAngkut(
                ongkosAngkut
        );

        result.setPriceActual(
                priceActual
        );

        result.setPriceEntered(
                priceEntered
        );

        result.setQtyEntered(
                qtyEntered
        );

        result.setLineNetAmt(
                lineNetAmt
        );


        return result;
    }


    // ============================================================
    // PRICE ACTUAL
    // ============================================================

    /**
     * PriceActual selalu harga satuan dalam
     * Base UOM Product.
     */
    public static BigDecimal calculatePriceActual(
            BigDecimal priceList,
            BigDecimal subsidiAmt,
            BigDecimal ongkosAngkut) {

        return nvl(priceList)
                .add(
                        nvl(subsidiAmt)
                )
                .add(
                        nvl(ongkosAngkut)
                );
    }


    // ============================================================
    // PRICE ENTERED
    // ============================================================

    /**
     * Convert PriceActual dari Product/Base UOM
     * menjadi PriceEntered dalam selected UOM.
     */
    public static BigDecimal calculatePriceEntered(
            MProduct product,
            int C_UOM_ID,
            BigDecimal priceActual) {

        if (product == null
                || product.get_ID() <= 0) {

            throw new AdempiereException(
                    "Product tidak ditemukan."
            );
        }


        priceActual =
                nvl(priceActual);


        int productUOM_ID =
                product.getC_UOM_ID();


        /*
         * Tidak perlu conversion.
         */
        if (C_UOM_ID <= 0
                || C_UOM_ID == productUOM_ID) {

            return priceActual;
        }


        /*
         * Convert:
         *
         * Base Product UOM price
         *
         * ->
         *
         * Entered UOM price
         *
         *
         * Contoh:
         *
         * Base UOM = KG
         * C_UOM_ID = KARUNG
         *
         * 1 KARUNG = 50 KG
         *
         * PriceActual = Rp 10.000 / KG
         *
         * PriceEntered =
         * Rp 500.000 / KARUNG
         */
        BigDecimal priceEntered =
                MUOMConversion.convertProductFrom(
                        product.getCtx(),
                        product.getM_Product_ID(),
                        C_UOM_ID,
                        priceActual,
                        PRICE_CONVERSION_PRECISION
                );


        if (priceEntered == null) {

            throw new AdempiereException(
                    "Konversi UOM untuk Product '"
                    + product.getName()
                    + "' tidak ditemukan."
            );
        }


        return priceEntered;
    }


    /**
     * Convenience overload jika hanya punya ID.
     */
    public static BigDecimal calculatePriceEntered(
            MOrder order,
            int M_Product_ID,
            int C_UOM_ID,
            BigDecimal priceActual,
            String trxName) {

        MProduct product =
                new MProduct(
                        order.getCtx(),
                        M_Product_ID,
                        trxName
                );


        return calculatePriceEntered(
                product,
                C_UOM_ID,
                priceActual
        );
    }


    // ============================================================
    // LINE NET AMT
    // ============================================================

    public static BigDecimal calculateLineNetAmt(
            MOrder order,
            BigDecimal priceEntered,
            BigDecimal qtyEntered) {

        BigDecimal lineNetAmt =
                nvl(priceEntered)
                        .multiply(
                                nvl(qtyEntered)
                        );


        /*
         * Sama seperti standard MOrderLine:
         * LineNetAmt mengikuti precision currency/order.
         */
        if (order != null) {

            int precision =
                    order.getPrecision();


            if (lineNetAmt.scale() > precision) {

                lineNetAmt =
                        lineNetAmt.setScale(
                                precision,
                                RoundingMode.HALF_UP
                        );
            }
        }


        return lineNetAmt;
    }


    // ============================================================
    // LAST INVOICE
    // ============================================================

    /**
     * Cari Last Completed/Closed AR Invoice:
     *
     * Same:
     * - Client
     * - Customer
     * - Product
     * - Currency
     *
     * Pricing source yang diambil:
     *
     * - PriceList
     * - subsidiAmt
     * - ongkosAngkut
     *
     * PriceActual dan PriceEntered TIDAK diambil karena
     * harus dihitung kembali sesuai UOM Proposal Retur saat ini.
     */
    public static LastInvoicePricing getLastInvoicePricing(
            MOrder order,
            int M_Product_ID,
            String trxName) {

        if (order == null
                || M_Product_ID <= 0) {

            return null;
        }


        String sql =
                " SELECT "
              + "     COALESCE(il.PriceList, 0), "
              + "     COALESCE(il.subsidiAmt, 0), "
              + "     COALESCE(il.ongkosAngkut, 0), "
              + "     i.C_Invoice_ID "
              + " FROM C_InvoiceLine il "
              + " INNER JOIN C_Invoice i "
              + "     ON i.C_Invoice_ID = il.C_Invoice_ID "
              + " WHERE i.AD_Client_ID = ? "
              + "   AND i.C_BPartner_ID = ? "
              + "   AND il.M_Product_ID = ? "
              + "   AND i.C_Currency_ID = ? "
              + "   AND i.IsSOTrx = 'Y' "
              + "   AND i.DocStatus IN ('CO','CL') "
              + "   AND i.IsActive = 'Y' "
              + "   AND il.IsActive = 'Y' "
              + " ORDER BY "
              + "     i.DateInvoiced DESC, "
              + "     i.C_Invoice_ID DESC, "
              + "     il.C_InvoiceLine_ID DESC ";


        PreparedStatement pstmt =
                null;

        ResultSet rs =
                null;


        try {

            pstmt =
                    DB.prepareStatement(
                            sql,
                            trxName
                    );


            /*
             * Cross-database friendly.
             * Tidak perlu LIMIT / TOP / FETCH FIRST.
             */
            pstmt.setMaxRows(1);


            int index =
                    1;


            pstmt.setInt(
                    index++,
                    order.getAD_Client_ID()
            );


            pstmt.setInt(
                    index++,
                    order.getC_BPartner_ID()
            );


            pstmt.setInt(
                    index++,
                    M_Product_ID
            );


            pstmt.setInt(
                    index++,
                    order.getC_Currency_ID()
            );


            rs =
                    pstmt.executeQuery();


            if (!rs.next()) {

                return null;
            }


            LastInvoicePricing result =
                    new LastInvoicePricing();


            result.setPriceList(
                    nvl(
                            rs.getBigDecimal(1)
                    )
            );


            result.setSubsidiAmt(
                    nvl(
                            rs.getBigDecimal(2)
                    )
            );


            result.setOngkosAngkut(
                    nvl(
                            rs.getBigDecimal(3)
                    )
            );


            result.setC_Invoice_ID(
                    rs.getInt(4)
            );


            return result;


        } catch (SQLException e) {

            throw new AdempiereException(
                    "Gagal mengambil Last Invoice "
                    + "untuk Product ID "
                    + M_Product_ID,
                    e
            );


        } finally {

            DB.close(
                    rs,
                    pstmt
            );
        }
    }


    // ============================================================
    // CURRENT PRICE LIST
    // ============================================================

    /**
     * Fallback kalau Last Invoice tidak ditemukan.
     *
     * Menggunakan:
     *
     * - M_PriceList_ID dari Order
     * - C_BPartner_ID Order
     * - DateOrdered Order
     * - QtyOrdered/base qty
     * - Transaction yang sama
     */
    public static BigDecimal getCurrentPriceList(
            MOrder order,
            int M_Product_ID,
            BigDecimal qtyOrdered,
            String trxName) {

        if (order == null
                || M_Product_ID <= 0
                || order.getM_PriceList_ID() <= 0) {

            return null;
        }


        qtyOrdered =
                nvl(qtyOrdered);


        /*
         * Kalau Product baru dipilih dan quantity
         * belum tersedia, gunakan 1 untuk pricing.
         */
        if (qtyOrdered.signum() == 0) {

            qtyOrdered =
                    Env.ONE;
        }


        MProductPricing pricing =
                new MProductPricing(
                        M_Product_ID,
                        order.getC_BPartner_ID(),
                        qtyOrdered,
                        order.isSOTrx(),
                        trxName
                );


        pricing.setM_PriceList_ID(
                order.getM_PriceList_ID()
        );


        if (order.getDateOrdered() != null) {

            pricing.setPriceDate(
                    order.getDateOrdered()
            );
        }


        if (!pricing.calculatePrice()) {

            return null;
        }


        return pricing.getPriceList();
    }


    // ============================================================
    // HELPERS
    // ============================================================

    public static BigDecimal nvl(
            BigDecimal value) {

        return value != null
                ? value
                : Env.ZERO;
    }


    // ============================================================
    // RESULT DTO
    // ============================================================

    /**
     * Full result untuk diterapkan oleh Callout
     * ataupun COrderLineEvent.
     */
    public static class PricingResult {

        private int M_Product_ID;

        private int C_UOM_ID;

        private BigDecimal qtyEntered =
                Env.ZERO;

        private BigDecimal priceList =
                Env.ZERO;

        private BigDecimal subsidiAmt =
                Env.ZERO;

        private BigDecimal ongkosAngkut =
                Env.ZERO;

        private BigDecimal priceActual =
                Env.ZERO;

        private BigDecimal priceEntered =
                Env.ZERO;

        private BigDecimal lineNetAmt =
                Env.ZERO;

        private boolean fromLastInvoice;

        private int C_Invoice_ID;


        public int getM_Product_ID() {
            return M_Product_ID;
        }


        public void setM_Product_ID(
                int M_Product_ID) {

            this.M_Product_ID =
                    M_Product_ID;
        }


        public int getC_UOM_ID() {
            return C_UOM_ID;
        }


        public void setC_UOM_ID(
                int C_UOM_ID) {

            this.C_UOM_ID =
                    C_UOM_ID;
        }


        public BigDecimal getQtyEntered() {
            return qtyEntered;
        }


        public void setQtyEntered(
                BigDecimal qtyEntered) {

            this.qtyEntered =
                    nvl(qtyEntered);
        }


        public BigDecimal getPriceList() {
            return priceList;
        }


        public void setPriceList(
                BigDecimal priceList) {

            this.priceList =
                    nvl(priceList);
        }


        public BigDecimal getSubsidiAmt() {
            return subsidiAmt;
        }


        public void setSubsidiAmt(
                BigDecimal subsidiAmt) {

            this.subsidiAmt =
                    nvl(subsidiAmt);
        }


        public BigDecimal getOngkosAngkut() {
            return ongkosAngkut;
        }


        public void setOngkosAngkut(
                BigDecimal ongkosAngkut) {

            this.ongkosAngkut =
                    nvl(ongkosAngkut);
        }


        public BigDecimal getPriceActual() {
            return priceActual;
        }


        public void setPriceActual(
                BigDecimal priceActual) {

            this.priceActual =
                    nvl(priceActual);
        }


        public BigDecimal getPriceEntered() {
            return priceEntered;
        }


        public void setPriceEntered(
                BigDecimal priceEntered) {

            this.priceEntered =
                    nvl(priceEntered);
        }


        public BigDecimal getLineNetAmt() {
            return lineNetAmt;
        }


        public void setLineNetAmt(
                BigDecimal lineNetAmt) {

            this.lineNetAmt =
                    nvl(lineNetAmt);
        }


        public boolean isFromLastInvoice() {
            return fromLastInvoice;
        }


        public void setFromLastInvoice(
                boolean fromLastInvoice) {

            this.fromLastInvoice =
                    fromLastInvoice;
        }


        public int getC_Invoice_ID() {
            return C_Invoice_ID;
        }


        public void setC_Invoice_ID(
                int C_Invoice_ID) {

            this.C_Invoice_ID =
                    C_Invoice_ID;
        }
    }


    // ============================================================
    // LAST INVOICE DTO
    // ============================================================

    public static class LastInvoicePricing {

        private BigDecimal priceList =
                Env.ZERO;

        private BigDecimal subsidiAmt =
                Env.ZERO;

        private BigDecimal ongkosAngkut =
                Env.ZERO;

        private int C_Invoice_ID;


        public BigDecimal getPriceList() {
            return priceList;
        }


        public void setPriceList(
                BigDecimal priceList) {

            this.priceList =
                    nvl(priceList);
        }


        public BigDecimal getSubsidiAmt() {
            return subsidiAmt;
        }


        public void setSubsidiAmt(
                BigDecimal subsidiAmt) {

            this.subsidiAmt =
                    nvl(subsidiAmt);
        }


        public BigDecimal getOngkosAngkut() {
            return ongkosAngkut;
        }


        public void setOngkosAngkut(
                BigDecimal ongkosAngkut) {

            this.ongkosAngkut =
                    nvl(ongkosAngkut);
        }


        public int getC_Invoice_ID() {
            return C_Invoice_ID;
        }


        public void setC_Invoice_ID(
                int C_Invoice_ID) {

            this.C_Invoice_ID =
                    C_Invoice_ID;
        }
    }
}