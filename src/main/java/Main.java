
import jssc.*;

import java.io.IOException;
public class Main {

    private static SerialPort serialPort;

    private static String portFinder()   {
        String stat ="failed - Port no found";
        SerialPortList serialPortList = new SerialPortList();
        for (String portlist :serialPortList.getPortNames()){
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
                while ((test == null) & (i<5000000)) {
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

    public static void main(String[] args) throws IOException {
        String port = portFinder();
        //Todo сделать проверку на "Failed"
        if (port.contains("failed")) System.out.println("AccelSensor in not connected...");
        else {
        serialPort = new SerialPort(portFinder());
        //todo допилить трап сендер
        TrapSender trap = new TrapSender();
        //trap.sendTrap("192.168.111.103"); //todo получить аргументы параметров запуска IP
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
    private static class PortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString(event.getEventValue());
                    //И снова отправляем запрос
                    System.out.println(data);
                    //    todo распарсить строку на x y z
                    //    todo сделать проверку на превышения порога получить из параметров запуска
                    //serialPort.writeString("Get data");
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
