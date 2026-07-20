public class tets {

    public static void main(String[] args){
        convertToTitle(1);
    }

    public static String convertToTitle(int columnNumber) {
        int f = 64 + columnNumber;
        char a = (char) f;
        System.out.println(a);
        return "A";
    }
}