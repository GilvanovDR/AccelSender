import jssc.*;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class Main {
    //private static Date date;
    private  static int X,Y,Z;
    private static SerialPort serialPort;
    private static void parcer(String str1){
       StringTokenizer st = new StringTokenizer(str1," ");
        if (st.countTokens()<3) {
            X = 0;
            Y = 0;
            Z = 0;

        }
        else {
            X = Integer.parseInt(st.nextToken());
            Y = Integer.parseInt(st.nextToken());
            Z = Integer.parseInt(st.nextToken());
        }

    }
    private static String portFinder()   {
        String stat ="failed - Port no found";
        System.out.println(new Date().toString() + " Searching...");
        for (String portlist : SerialPortList.getPortNames()){
            serialPort = new SerialPort(portlist);
            String test;
            //System.out.println(portlist + " search...");
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                        SerialPort.FLOWCONTROL_RTSCTS_OUT);
                TimeUnit.SECONDS.sleep(5);
                test = serialPort.readString();
                serialPort.closePort();
               // System.out.println(test);
                    if (test != null)
                        if (test.contains("accelstart")){
                            stat = portlist;
                            System.out.println(new Date().toString() + " " + portlist + " successfully...");
                            break;
                        }
                //System.out.println(portlist + " not found...");

            }
            catch (SerialPortException ex) {
                System.out.println(new Date().toString() + " " + portlist + " not found...");
                stat ="failed " + ex.toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return stat;
    }
    private static String IPADRES;
    private static int SENSYTY;
    private static TrapSender TRAP = new TrapSender();
    private static String PORT = "";

    public static void main(String[] args) throws IOException {
       /* for (String stt : args) {
            System.out.println(stt);
        }*/
        if (args.length == 0) System.out.println(new Date().toString() + " incorrect start key...");

        if (args.length == 1) { //findCom
            if (args[0].equals("findCom")) System.out.println(new Date().toString() + " Accel port is " + portFinder());
            else System.out.println(new Date().toString() + " incorrect start key...");
        }

        if (args.length == 2) { //IP SENSYTIVE
            IPADRES = args[0];
            SENSYTY = Integer.parseInt(args[1]);
            PORT = portFinder();
            ComOpener();
        }

        if (args.length == 3){ //SNMP TEXT
            if (args[0].equals("snmp")) {
                IPADRES = args[1];
                try {
                    TRAP.sendTrap(IPADRES, args[2]);
                } catch (IllegalArgumentException e) {
                    System.out.println("incorrect IP :" + e);
                }
            }
            else { //IP SENSYTIVE COM
                StringTokenizer st = new StringTokenizer(args[0], ".");
                if (st.countTokens() == 4) {
                    IPADRES = args[0];
                    SENSYTY = Integer.parseInt(args[1]);
                    PORT = args[2];
                    ComOpener();
                }
                else System.out.println("incorrect start key...");
            }
        }

        if (args.length > 3) {
            System.out.println(new Date().toString() + " incorrect start key...");
        }
    }

    private static void ComOpener() {
        if (PORT.contains("failed")) System.out.println( new Date().toString() + " AccelSensor in not connected...");
        else {
            System.out.println();
            System.out.println("\t" +"Sens" + "\t\t" + SENSYTY);
            System.out.println("\t" +"Port" + "\t\t" + PORT);
            System.out.println("\t" +"Snmp IP" +  "\t\t" + IPADRES);
            System.out.println();
            System.out.println( new Date().toString() + " Accel is start...");
            serialPort = new SerialPort(PORT);
            try {
                serialPort.openPort();
                //Выставляем параметры
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                //Включаем аппаратное управление потоком
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                        SerialPort.FLOWCONTROL_RTSCTS_OUT);
                //Устанавливаем ивент лисенер и маску
                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            } catch (SerialPortException ex) {
                //System.out.println("failed " + ex);
                System.out.println(new Date().toString() + " Accel on " + PORT + " not found");
            }
        }
    }
    private static class PortReader implements SerialPortEventListener{
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    parcer(serialPort.readString(event.getEventValue()));
                    if ((X>SENSYTY)||(Y>SENSYTY)||(Z>SENSYTY)) {
                        System.out.println(new Date().toString() + " Move detect");
                        try {
                            TRAP.sendTrap(IPADRES,"Move detect");
                        } catch (IOException e) {
                           // e.printStackTrace();
                            //System.out.println(e);
                        }
                    }
                   // System.out.println(""+ X + Y + Z);
                }
                catch (SerialPortException ex) {
                    //System.out.println(ex);
                }
            }
        }
    }
}
