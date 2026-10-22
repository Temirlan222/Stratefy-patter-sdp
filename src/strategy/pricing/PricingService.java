package strategy.pricing;

public class PricingService {
    public int calculatePrice(
            String mode,
            int basePrice,
            int reservedSeats,
            int totalSeats
    ){
        switch (mode){
            case "standard":
                return basePrice;
            case "demand":
                if (reservedSeats*2 >= totalSeats){
                    double ret = basePrice * 1.2;
                    return (int) ret;

                }
                else{
                    return basePrice;
                }
            case "booking_fee":
                return basePrice + 500;

            default:
                throw new IllegalArgumentException("Передан неизвестный режим");
        }

    }
}
