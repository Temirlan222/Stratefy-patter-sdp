package strategy.pricing;

public interface PricingStrategy {
    int calculatePrice(int basePrice, int reservedSeats, int totalSeats);

}
