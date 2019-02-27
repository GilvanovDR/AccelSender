import jssc.SerialPort;
import jssc.SerialPortException;
import java.util.Date;

class PortFinderThread extends Thread{
    private String nameThread;
    private boolean treadStatusFinish;
    private boolean port = false;
    PortFinderThread(String name){
        nameThread = name;
        treadStatusFinish = false;
    }

    @Override
    public void run() {
        System.out.println(new Date().toString() + " " + nameThread + "- Start searching...");
        SerialPort serialPort = new SerialPort(nameThread);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
            Thread.sleep(3000);
            String comBuffer = serialPort.readString();
            //System.out.println(comBuffer);
            serialPort.closePort();
            if ((comBuffer != null)&&(comBuffer.contains("accel"))){
                System.out.println(new Date().toString() + " " + nameThread + " successfully");
                port = true;
            }
        } catch (SerialPortException e) {
            //e.printStackTrace();
            System.out.println(new Date().toString() + " " + nameThread + " " + e);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            System.out.println(new Date().toString() + " " + nameThread + " " + e);
        }
        System.out.println(new Date().toString() + " " + nameThread + " searching stop");
        treadStatusFinish = true;


    }
    public boolean getPort() {
        return port;
    }

    public String getNameThread() {
        return nameThread;
    }

    public boolean isTreadStatusFinish() {
        return treadStatusFinish;
    }
}
