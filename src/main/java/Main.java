import jssc.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class Main {
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
        ArrayList<PortFinderThread> arrayList = new ArrayList<PortFinderThread>();
        int i = 0;
        for (String portList : SerialPortList.getPortNames()) {
            arrayList.add(new PortFinderThread(portList));
            arrayList.get(i++).start();

        }
        boolean statusTread = false;
        while (!statusTread) {
            statusTread = true;
            for (int j = 0; j < i; j++) {
                if (!arrayList.get(j).isTreadStatusFinish()) statusTread = false;
            }
        }
        for (int j = 0; j < i; j++){
            if (arrayList.get(j).getPort())
            return arrayList.get(j).getNameThread();
        }
        return "not found";
    }
    private static String IP_ADDRESS;
    private static int SENSITIVE;
    private static TrapSender TRAP = new TrapSender();
    private static String PORT = "";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) System.out.println(new Date().toString() + " incorrect start key...");
        if (args.length == 1) { //findCom
            if (args[0].equals("findCom")) System.out.println(new Date().toString() + " Accel port is " + portFinder());
            else System.out.println(new Date().toString() + " incorrect start key...");
        }
        if (args.length == 2) { //IP SENSITIVE
            IP_ADDRESS = args[0];
            SENSITIVE = Integer.parseInt(args[1]);
            PORT = portFinder();
            ComOpener();
        }
        if (args.length == 3){ //SNMP TEXT
            if (args[0].equals("snmp")) {
                IP_ADDRESS = args[1];
                try {
                    TRAP.sendTrap(IP_ADDRESS, args[2]);
                } catch (IllegalArgumentException e) {
                    System.out.println("incorrect IP :" + e);
                }
            }
            else { //IP SENSITIVE COM
                StringTokenizer st = new StringTokenizer(args[0], ".");
                if (st.countTokens() == 4) {
                    IP_ADDRESS = args[0];
                    SENSITIVE = Integer.parseInt(args[1]);
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
            System.out.println("\t" +"Sens" + "\t\t" + SENSITIVE);
            System.out.println("\t" +"Port" + "\t\t" + PORT);
            System.out.println("\t" +"Snmp IP" +  "\t\t" + IP_ADDRESS);
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
                    if ((X> SENSITIVE)||(Y> SENSITIVE)||(Z> SENSITIVE)) {
                        System.out.println(new Date().toString() + " Move detect");
                        try {
                            TRAP.sendTrap(IP_ADDRESS,"Move detect");
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
