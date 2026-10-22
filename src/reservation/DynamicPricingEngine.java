package reservation;

public class DynamicPricingEngine {
    public int calculatePrice(int basePrice, int reservedSeats, int totalSeats){
        if (reservedSeats * 2 >= totalSeats){
            return (int) (basePrice * 1.2);
        }

        return basePrice;
    }
}
