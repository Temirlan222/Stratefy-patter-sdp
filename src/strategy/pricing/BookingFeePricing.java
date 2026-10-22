package strategy.pricing;

public class BookingFeePricing implements PricingStrategy{
    @Override
    public int calculatePrice(int basePrice, int reservedSeats, int totalSeats){
        return basePrice+500;
    }
}
