public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Please input the k: ");
        int k = Integer.parseInt(args[0]); 
        if(k > 0) {
            LARA lara = new LARA(k, "input/seq1.txt", "input/seq2.txt", "input/rnd.txt");
            lara.execute();
        } else {
            System.out.println("k must be greater than 0");
        }
    }
}
