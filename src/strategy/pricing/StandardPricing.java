package strategy.pricing;

public class StandardPricing implements PricingStrategy{
    @Override
    public int calculatePrice(int basePrice, int reservedSeats, int totalSeats){
        return basePrice;
    }
}
