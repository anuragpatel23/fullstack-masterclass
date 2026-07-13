/**
 * TOPIC: OOPs — all four pillars + interview traps in one runnable file.
 * Real-life model used: Vehicles & payment processing.
 */
import java.util.List;

public class OopsDemo {

    public static void main(String[] args) {
        // ---- Runtime polymorphism (dynamic dispatch) ----
        List<Vehicle> fleet = List.of(new PetrolCar(), new ElectricCar());
        fleet.forEach(v -> System.out.println(v.describe())); // actual type decides

        // ---- Compile-time polymorphism (overloading) ----
        Printer p = new Printer();
        p.print(42);        // int version
        p.print("42");      // String version
        p.print((Object) "42"); // Object version — chosen by REFERENCE type!

        // ---- Method hiding trap ----
        Vehicle v = new PetrolCar();
        System.out.println(Vehicle.category());   // static: resolved by class, not object

        // ---- Composition over inheritance ----
        var checkout = new Checkout(new UpiPayment());   // swap strategy at runtime
        checkout.pay(499.0);
        var checkout2 = new Checkout(new CardPayment());
        checkout2.pay(499.0);

        // ---- Diamond problem with default methods ----
        new SmartDevice().connect();
    }

    // ===== Abstraction + Inheritance =====
    static abstract class Vehicle {
        // ENCAPSULATION: state is private, exposed via behavior only
        private int fuelPercent = 100;
        protected int fuel() { return fuelPercent; }

        abstract String engineType();                       // contract for children
        String describe() {                                 // template using abstraction
            return getClass().getSimpleName() + " runs on " + engineType()
                 + " (fuel " + fuel() + "%)";
        }
        static String category() { return "GenericVehicle"; } // hidden, not overridden
    }

    static class PetrolCar extends Vehicle {
        @Override String engineType() { return "petrol"; }
        static String category() { return "PetrolVehicle"; }  // HIDES parent static
    }
    static class ElectricCar extends Vehicle {
        @Override String engineType() { return "battery"; }
    }

    // ===== Overloading resolved at compile time =====
    static class Printer {
        void print(int x)    { System.out.println("int: " + x); }
        void print(String x) { System.out.println("String: " + x); }
        void print(Object x) { System.out.println("Object: " + x); }
    }

    // ===== Composition + Dependency Inversion (SOLID: D, O) =====
    interface PaymentMethod { void process(double amount); }           // abstraction
    static class UpiPayment implements PaymentMethod {
        public void process(double amt) { System.out.println("UPI paid ₹" + amt); }
    }
    static class CardPayment implements PaymentMethod {
        public void process(double amt) { System.out.println("Card paid ₹" + amt); }
    }
    /** Checkout HAS-A PaymentMethod; add new methods without modifying Checkout (OCP). */
    static class Checkout {
        private final PaymentMethod method;      // injected dependency
        Checkout(PaymentMethod method) { this.method = method; }
        void pay(double amount) { method.process(amount); }
    }

    // ===== Diamond problem: compiler FORCES resolution =====
    interface Wifi      { default void connect() { System.out.println("wifi"); } }
    interface Bluetooth { default void connect() { System.out.println("bluetooth"); } }
    static class SmartDevice implements Wifi, Bluetooth {
        @Override public void connect() {
            Wifi.super.connect();               // explicit choice resolves ambiguity
        }
    }
}
