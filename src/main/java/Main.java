
import com.sun.org.apache.xpath.internal.SourceTree;
import jssc.*;
import java.io.IOException;
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
        String stat ="failed - Port no found";
        for (String portlist : SerialPortList.getPortNames()){
            serialPort = new SerialPort(portlist);
            String test;
            System.out.println(portlist + " search...");
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                        SerialPort.FLOWCONTROL_RTSCTS_OUT);
                test = serialPort.readString();
                int i = 0;
                while ((test == null) & (i<5000000)) { //Задержка - сколько?
                    test = serialPort.readString();
                    i++;
                }
                serialPort.closePort();
                if ((test != null)&&(test.contains("accelstart"))) {
                    stat = portlist;
                    System.out.println(portlist + " successfully...");
                    break;
                }
                System.out.println(portlist + " not found...");

            }
            catch (SerialPortException ex) {
                System.out.println(portlist + " not found...");
                stat ="failed " + ex.toString();
            }
        }
        return stat;
    }
    private static String IPADRES;
    private static int SENSYTY =100;
    private static TrapSender TRAP = new TrapSender();

    public static void main(String[] args) throws IOException {
        for (String stt : args) {
            System.out.println(stt);
        }
        if (args.length == 0) System.out.println("incorrect start key...");
        if (args.length == 1)
            if (args[0].equals("findCom")) System.out.println("Accel port is " + portFinder());
            else System.out.println("incorrect start key...");
        if (args.length == 3)
            if (args[0].equals("snmp")) {
                IPADRES = args[1];
                try {
                    TRAP.sendTrap(IPADRES, args[2]);
                } catch (IllegalArgumentException e) {
                    System.out.println("incorrect IP :" + e);
                }

            }
        //todo Сделать порт сендер отдельным статисческим классом
        //todo "Аргументы -Ip -sensytive  установки для SnmpTrap + автопоиск Com
        //todo "Аргументы -Ip -sensytive  установки для SnmpTrap + COM
        String port = portFinder();
        if (port.contains("failed")) System.out.println("AccelSensor in not connected...");
        else {
        serialPort = new SerialPort(port);
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
        }
        catch (SerialPortException ex) {
            System.out.println("failed " + ex);
        }
    }}
    private static class PortReader implements SerialPortEventListener{
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    parcer(serialPort.readString(event.getEventValue()));
                    if ((X>SENSYTY)||(Y>SENSYTY)||(Z>SENSYTY)) {
                        try {
                            TRAP.sendTrap(IPADRES,"Move detect");
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println(e);
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
