package strategy.pricing;

public class PricingContext {
    private PricingStrategy strategy;

    public PricingContext(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public int calculatePrice(int basePrice, int reservedSeats, int totalSeats){
        return strategy.calculatePrice(basePrice,reservedSeats,totalSeats);
    }


}
