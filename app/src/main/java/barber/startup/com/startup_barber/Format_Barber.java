package barber.startup.com.startup_barber;

/**
 * Created by ayush on 9/3/16.
 */
public class Format_Barber {
    private int price=0;
    private int time=0;
    private String barber="Barber Name";
    private int barberId=-1;


    public int getTime() {
        return time;
    }

    public int getPrice() {
        return price;
    }

    public int getBarberId() {
        return barberId;
    }

    public String getBarber() {
        return barber;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setBarber(String barber) {
        this.barber = barber;
    }

    public void setBarberId(int barberId) {
        this.barberId = barberId;
    }
}
