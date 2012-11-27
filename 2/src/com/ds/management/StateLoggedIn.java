package com.ds.management;

import java.rmi.RemoteException;
import java.util.List;

import com.ds.billing.AuctionBill;
import com.ds.billing.BillingServerSecure;
import com.ds.billing.PriceSteps;
import com.ds.billing.PriceStep;
import com.ds.management.ManagementMain.Data;


/**
 * Handles user input while the user is logged in.
 */
class StateLoggedIn extends State {

    private final String user;
    private final BillingServerSecure billing;

    public StateLoggedIn(Data data, String user, BillingServerSecure billing) {
        super(data);
        this.user = user;
        this.billing = billing;
    }

    @Override
    public State processCommand(CommandMatcher.Type type, List<String> args) {
        State next = super.processCommand(type, args);
        if (next != null) {
            return next;
        }

        switch (type) {
            case LOGOUT:
                System.out.printf("%s successfully logged out%n", user);
                return new StateLoggedOut(getData());
            case STEPS:
                List<PriceStep> priceSteps;
                try {
                    priceSteps = billing.getPriceSteps().getPriceSteps();

                    System.out.println("Min_Price Max_Price Fee_Fixed Fee_Variable");
                    for (PriceStep p : priceSteps) {
                        System.out.printf("%1$-9.2f %2$-9.2f %3$-9.2f %4$-9s%n",
                                p.getStartPrice(), p.getEndPrice(),
                                p.getFixedPrice(),
                                String.format("%.2f%%", p.getVariablePricePercent()));
                    }
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getLocalizedMessage());
                }

                return this;
            case ADD_STEP:
                try {
                    double startPrice = Double.parseDouble(args.get(0));
                    double endPrice = Double.parseDouble(args.get(1));
                    if (endPrice == 0) {
                        endPrice = Double.POSITIVE_INFINITY;
                    }

                    billing.createPriceStep(
                            startPrice,
                            endPrice,
                            Double.parseDouble(args.get(2)),
                            Double.parseDouble(args.get(3)));

                    System.out.printf("Step [%.2f %.2f] successfully added%n", startPrice,
                            endPrice);
                } catch (NumberFormatException e) {
                    System.out.println("Only non-negative numbers allowed.");
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getLocalizedMessage());
                }

                return this;
            case REM_STEP:
                try {
                    double startPrice = Double.parseDouble(args.get(0));
                    double endPrice = Double.parseDouble(args.get(1));
                    if (endPrice == 0) {
                        endPrice = Double.POSITIVE_INFINITY;
                    }

                    billing.deletePriceStep(
                            startPrice,
                            endPrice);

                    System.out.printf("Price Step [%.2f %.2f] successfully removed%n", startPrice,
                            endPrice);
                } catch (NumberFormatException e) {
                    System.out.println("Only non-negative numbers allowed.");
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getLocalizedMessage());
                }

                return this;
            case BILL:
                List<AuctionBill> bills;
                try {
                    bills = billing.getBill(args.get(0)).getAuctionBills();

                    System.out.println("auction_ID strike_price fee_fixed fee_variable fee_total");
                    for (AuctionBill b : bills) {
                        System.out.printf("%1$-10d %2$-12.2f %3$-9.2f %4$-12.2f %5$-9.2f%n",
                                b.getAuctionID(), b.getStrikePrice(), b.getFeeFixed(),
                                b.getFeeVariable(), b.getFeeTotal());
                    }
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getLocalizedMessage());
                }

                return this;
            default:
                throw new IllegalArgumentException(String.format(
                        "Invalid command in state %s", StateLoggedIn.class.getName()));
        }
    }

    @Override
    public String getPrefix() {
        return String.format("%s> ", user);
    }

}
