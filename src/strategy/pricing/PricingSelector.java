package strategy.pricing;

public class PricingSelector {
    public PricingStrategy selectByName(String mode){
        switch (mode){
            case "standard":
                return new StandardPricing();
            case "demand":
                return new DemandPricing();
            case "booking_fee":
                return new BookingFeePricing();

            default:
                throw new IllegalArgumentException("Передан неизвестный режим");
        }

    }

    public PricingStrategy select(String mode,boolean partnerBooking, int reservedSeats,int totalSeats){
        if(mode.equals("auto")){
            return selectAutomatically(partnerBooking,reservedSeats,totalSeats);
        }
        else{
            return selectByName(mode);
        }
    }

    public PricingStrategy selectAutomatically(boolean partnerBooking, int reservedSeats, int totalSeats){
        if (partnerBooking == true){
            return new BookingFeePricing();
        }
        else if (reservedSeats*2 >= totalSeats) {
            return new DemandPricing();

        }
        else{
            return new StandardPricing();
        }
    }
}
