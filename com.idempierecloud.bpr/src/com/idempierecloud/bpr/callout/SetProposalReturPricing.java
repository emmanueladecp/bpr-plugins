package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MOrder;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomCallout;
import com.idempierecloud.bpr.util.ProposalReturPricingUtil;
import com.idempierecloud.bpr.util.ProposalReturPricingUtil.PricingResult;

public class SetProposalReturPricing extends CustomCallout {

    /*
     * Sesuaikan import/static constant dengan project Anda.
     *
     * Saya asumsikan constant ini memang sudah ada:
     *
     * C_DocType_ID_CustomerReturnBPR
     */

    private static final String CTX_RECALCULATING =
            "BPR_ProposalReturPricing_Recalculating";
    
    private static final int C_DocType_ID_CustomerReturnBPR = 
    		1000084;


    // ============================================================
    // START
    // ============================================================

    @Override
    protected String start() {

        /*
         * Mencegah recursive callout karena di bawah kita
         * melakukan setValue() terhadap field lain.
         */
        if (isRecalculating()) {
            return null;
        }


        MOrder order =
                getCurrentOrder();


        if (order == null) {
            return null;
        }


        /*
         * Jangan sentuh Sales Order / Order Line biasa.
         */
        if (!isProposalRetur(order)) {
            return null;
        }


        String columnName =
                getColumnName();


        // ========================================================
        // PRODUCT
        // ========================================================

        if (I_C_OrderLine.COLUMNNAME_M_Product_ID
                .equals(columnName)) {

            return productChanged(
                    order
            );
        }


        // ========================================================
        // SOURCE PRICE
        //
        // Semua perubahan source menyebabkan:
        //
        // PriceActual
        // PriceEntered
        // LineNetAmt
        //
        // dihitung ulang.
        // ========================================================

        if (I_C_OrderLine.COLUMNNAME_PriceList
                .equals(columnName)
                ||
                ProposalReturPricingUtil.COLUMN_SUBSIDI_AMT
                        .equals(columnName)
                ||
                ProposalReturPricingUtil.COLUMN_ONGKOS_ANGKUT
                        .equals(columnName)) {

            return pricingSourceChanged(
                    order
            );
        }


        // ========================================================
        // UOM
        //
        // PriceActual tidak berubah.
        //
        // Tapi PriceEntered harus mengikuti UOM baru.
        // ========================================================

        if (I_C_OrderLine.COLUMNNAME_C_UOM_ID
                .equals(columnName)) {

            return uomChanged(
                    order
            );
        }


        // ========================================================
        // QTY
        //
        // PriceActual dan PriceEntered tetap.
        //
        // Hanya LineNetAmt berubah.
        // ========================================================

        if (I_C_OrderLine.COLUMNNAME_QtyEntered
                .equals(columnName)) {

            return qtyEnteredChanged(
                    order
            );
        }


        return null;
    }


    // ============================================================
    // PRODUCT CHANGED
    // ============================================================

    /**
     * Product berubah.
     *
     * Di sini PricingUtil akan:
     *
     * 1. Cari Last Invoice
     * 2. Kalau tidak ada -> Current Price List
     * 3. Hitung PriceActual
     * 4. Convert PriceEntered berdasarkan C_UOM_ID
     * 5. Hitung LineNetAmt
     */
    private String productChanged(
            MOrder order) {

        int M_Product_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_M_Product_ID
                );


        if (M_Product_ID <= 0) {
            return null;
        }


        int C_UOM_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_C_UOM_ID
                );


        BigDecimal qtyEntered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_QtyEntered
                );


        BigDecimal qtyOrdered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_QtyOrdered
                );


        try {

            PricingResult pricing =
                    ProposalReturPricingUtil
                            .resolveInitialPricing(
                                    order,
                                    M_Product_ID,
                                    C_UOM_ID,
                                    qtyEntered,
                                    qtyOrdered,
                                    getTrxName()
                            );


            setRecalculating(true);

            try {

                /*
                 * Kalau UOM belum terisi,
                 * Util fallback ke Base UOM.
                 */
                if (C_UOM_ID <= 0
                        && pricing.getC_UOM_ID() > 0) {

                    setValue(
                            I_C_OrderLine.COLUMNNAME_C_UOM_ID,
                            pricing.getC_UOM_ID()
                    );
                }


                // =================================================
                // SOURCE
                // =================================================

                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceList,
                        pricing.getPriceList()
                );


                setValue(
                        ProposalReturPricingUtil
                                .COLUMN_SUBSIDI_AMT,
                        pricing.getSubsidiAmt()
                );


                setValue(
                        ProposalReturPricingUtil
                                .COLUMN_ONGKOS_ANGKUT,
                        pricing.getOngkosAngkut()
                );


                // =================================================
                // DERIVED
                // =================================================

                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceActual,
                        pricing.getPriceActual()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceEntered,
                        pricing.getPriceEntered()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_LineNetAmt,
                        pricing.getLineNetAmt()
                );


                return null;

            } finally {

                setRecalculating(false);
            }


        } catch (Exception e) {

            return e.getMessage();
        }
    }


    // ============================================================
    // SOURCE PRICING CHANGED
    // ============================================================

    /**
     * PriceList / subsidiAmt / ongkosAngkut berubah.
     *
     * Tidak query Last Invoice lagi.
     *
     * Kita menggunakan nilai yang sedang ada pada GridTab.
     */
    private String pricingSourceChanged(
            MOrder order) {

        int M_Product_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_M_Product_ID
                );


        if (M_Product_ID <= 0) {
            return null;
        }


        int C_UOM_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_C_UOM_ID
                );


        BigDecimal qtyEntered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_QtyEntered
                );


        BigDecimal priceList =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_PriceList
                );


        BigDecimal subsidiAmt =
                getTabValueAsBD(
                        ProposalReturPricingUtil
                                .COLUMN_SUBSIDI_AMT
                );


        BigDecimal ongkosAngkut =
                getTabValueAsBD(
                        ProposalReturPricingUtil
                                .COLUMN_ONGKOS_ANGKUT
                );


        try {

            PricingResult pricing =
                    ProposalReturPricingUtil
                            .calculateDerivedPricing(
                                    order,
                                    M_Product_ID,
                                    C_UOM_ID,
                                    qtyEntered,
                                    priceList,
                                    subsidiAmt,
                                    ongkosAngkut,
                                    getTrxName()
                            );


            setRecalculating(true);

            try {

                /*
                 * Source field tidak perlu diset ulang.
                 *
                 * User sedang mengubah field tersebut.
                 *
                 * Kita hanya update derived fields.
                 */

                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceActual,
                        pricing.getPriceActual()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceEntered,
                        pricing.getPriceEntered()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_LineNetAmt,
                        pricing.getLineNetAmt()
                );


                return null;

            } finally {

                setRecalculating(false);
            }


        } catch (Exception e) {

            return e.getMessage();
        }
    }


    // ============================================================
    // UOM CHANGED
    // ============================================================

    /**
     * UOM berubah.
     *
     * Source tidak berubah:
     *
     * PriceList
     * subsidiAmt
     * ongkosAngkut
     *
     * PriceActual juga pada dasarnya sama.
     *
     * Yang berubah terutama:
     *
     * PriceEntered
     * LineNetAmt
     */
    private String uomChanged(
            MOrder order) {

        int M_Product_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_M_Product_ID
                );


        if (M_Product_ID <= 0) {
            return null;
        }


        int C_UOM_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_C_UOM_ID
                );


        if (C_UOM_ID <= 0) {
            return null;
        }


        BigDecimal qtyEntered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_QtyEntered
                );


        BigDecimal priceList =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_PriceList
                );


        BigDecimal subsidiAmt =
                getTabValueAsBD(
                        ProposalReturPricingUtil
                                .COLUMN_SUBSIDI_AMT
                );


        BigDecimal ongkosAngkut =
                getTabValueAsBD(
                        ProposalReturPricingUtil
                                .COLUMN_ONGKOS_ANGKUT
                );


        try {

            PricingResult pricing =
                    ProposalReturPricingUtil
                            .calculateDerivedPricing(
                                    order,
                                    M_Product_ID,
                                    C_UOM_ID,
                                    qtyEntered,
                                    priceList,
                                    subsidiAmt,
                                    ongkosAngkut,
                                    getTrxName()
                            );


            setRecalculating(true);

            try {

                /*
                 * Sebenarnya PriceActual tidak berubah,
                 * tetapi kita set kembali supaya selalu
                 * sinkron dengan formula yang sama.
                 */
                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceActual,
                        pricing.getPriceActual()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_PriceEntered,
                        pricing.getPriceEntered()
                );


                setValue(
                        I_C_OrderLine.COLUMNNAME_LineNetAmt,
                        pricing.getLineNetAmt()
                );


                return null;

            } finally {

                setRecalculating(false);
            }


        } catch (Exception e) {

            return e.getMessage();
        }
    }


    // ============================================================
    // QTY ENTERED CHANGED
    // ============================================================

    /**
     * QtyEntered tidak mempengaruhi PriceEntered.
     *
     * PriceEntered adalah harga PER selected UOM.
     *
     * Jadi hanya:
     *
     * LineNetAmt = PriceEntered * QtyEntered
     */
    private String qtyEnteredChanged(
            MOrder order) {

        BigDecimal priceEntered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_PriceEntered
                );


        BigDecimal qtyEntered =
                getTabValueAsBD(
                        I_C_OrderLine.COLUMNNAME_QtyEntered
                );


        BigDecimal lineNetAmt =
                ProposalReturPricingUtil
                        .calculateLineNetAmt(
                                order,
                                priceEntered,
                                qtyEntered
                        );


        setRecalculating(true);

        try {

            setValue(
                    I_C_OrderLine.COLUMNNAME_LineNetAmt,
                    lineNetAmt
            );

            return null;

        } finally {

            setRecalculating(false);
        }
    }


    // ============================================================
    // CURRENT ORDER
    // ============================================================

    /**
     * PENTING:
     *
     * Jangan menggunakan:
     *
     * new MOrder(ctx, id, null)
     *
     * karena Proposal Retur adalah Transaction Window.
     *
     * Header C_Order mungkin belum committed tetapi sudah
     * mempunyai C_Order_ID.
     */
    private MOrder getCurrentOrder() {

        int C_Order_ID =
                getTabValueAsInt(
                        I_C_OrderLine.COLUMNNAME_C_Order_ID
                );


        if (C_Order_ID <= 0) {
            return null;
        }


        MOrder order =
                new MOrder(
                        getCtx(),
                        C_Order_ID,
                        getTrxName()
                );


        if (order.get_ID() <= 0) {
            return null;
        }


        return order;
    }


    // ============================================================
    // DOCUMENT TYPE CHECK
    // ============================================================

    private boolean isProposalRetur(
            MOrder order) {

        if (order == null) {
            return false;
        }


        return order.getC_DocTypeTarget_ID()
                == C_DocType_ID_CustomerReturnBPR;
    }


    // ============================================================
    // TRANSACTION
    // ============================================================

    private String getTrxName() {

        if (getTab() == null) {
            return null;
        }


        if (getTab().getTableModel() == null) {
            return null;
        }


        return getTab()
                .getTableModel()
                .get_TrxName();
    }


    // ============================================================
    // GRID TAB HELPERS
    // ============================================================

    private int getTabValueAsInt(
            String columnName) {

        Object value =
                getTab().getValue(
                        columnName
                );


        if (value == null) {
            return 0;
        }


        if (value instanceof Number) {

            return ((Number) value)
                    .intValue();
        }


        try {

            return Integer.parseInt(
                    value.toString()
            );

        } catch (Exception e) {

            return 0;
        }
    }


    private BigDecimal getTabValueAsBD(
            String columnName) {

        Object value =
                getTab().getValue(
                        columnName
                );


        if (value == null) {
            return Env.ZERO;
        }


        if (value instanceof BigDecimal) {

            return (BigDecimal) value;
        }


        if (value instanceof Number) {

            return new BigDecimal(
                    value.toString()
            );
        }


        try {

            return new BigDecimal(
                    value.toString()
            );

        } catch (Exception e) {

            return Env.ZERO;
        }
    }


    // ============================================================
    // RECURSION GUARD
    // ============================================================

    private boolean isRecalculating() {

        return "Y".equals(
                Env.getContext(
                        getCtx(),
                        getWindowNo(),
                        CTX_RECALCULATING
                )
        );
    }


    private void setRecalculating(
            boolean recalculating) {

        Env.setContext(
                getCtx(),
                getWindowNo(),
                CTX_RECALCULATING,
                recalculating
                        ? "Y"
                        : ""
        );
    }
}