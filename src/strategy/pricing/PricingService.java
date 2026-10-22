package strategy.pricing;

public class PricingService {
    public int calculatePrice(
            String mode,
            int basePrice,
            int reservedSeats,
            int totalSeats
    ){
        PricingSelector selector = new PricingSelector();
        PricingStrategy strategy = selector.selectByName(mode);
        PricingContext context = new PricingContext(strategy);
        return context.calculatePrice(basePrice, reservedSeats, totalSeats);
    }

}
