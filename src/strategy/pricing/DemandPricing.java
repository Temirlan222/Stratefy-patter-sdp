package strategy.pricing;

public class DemandPricing implements PricingStrategy{
    @Override
    public int calculatePrice(int basePrice, int reservedSeats, int totalSeats){
        if (reservedSeats*2 >= totalSeats){
            double ret = basePrice * 1.2;
            return (int) ret;

        }
        else{
            return basePrice;
        }
    }

}
