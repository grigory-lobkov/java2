package ru.progwards.java2.lessons.http.bankomat;

import ru.progwards.java2.lessons.http.bankomat.model.Account;
import ru.progwards.java2.lessons.http.bankomat.service.AccountService;
import ru.progwards.java2.lessons.http.bankomat.service.StoreService;
import ru.progwards.java2.lessons.http.bankomat.service.impl.ConcurrentAccountService;
import ru.progwards.java2.lessons.http.bankomat.service.impl.FileStoreService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Scanner;

//http://localhost/balance?id=3d4989c7-bf05-4450-89a9-3aaa1c43d74c
//http://localhost/deposit?id=3d4989c7-bf05-4450-89a9-3aaa1c43d74c&amount=1000

public class AtmHttpServer {

    final static int SERVER_PORT = 80;

    final static StoreService ss = new FileStoreService();
    final static AccountService as = new ConcurrentAccountService(ss);

    public static void main(String[] args) {


        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            while (true) {
                // wait for next client
                Socket socket = serverSocket.accept();
                // make process thread
                new Thread(new RequestHandler(socket, ss, as)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



class RequestHandler implements Runnable {

    final static String getPrefix = "GET /";

    final static String hostPrefix = "hostname: ";

    final static String responceOk =
            "HTTP/1.1 200 ok\n" +
            "Content-Type: text/html; charset=utf-8\n" +
            "Content-Length: {length}\n\n" +
            "{text}";

    final static String responceError =
            "HTTP/1.1 400\n\n"+
            "{text}";

    final static int SOCKET_TIMEOUT_MS = 1000; // 50 is Ok

    Socket socket;
    String threadName;
    final boolean log = false;
    String sendText = "";
    StoreService ss;
    AccountService as;
    InputStream is;
    OutputStream os;

    // parameters to call method
    String host = "";
    String method = "";
    Hashtable<String, String> params = new Hashtable<>();

    public RequestHandler(Socket socket, StoreService ss, AccountService as) {
        this.socket = socket;
        this.ss = ss;
        this.as = as;
    }

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        if (log) System.out.println(threadName + " AtmHttpServer.RequestHandler.run()");
        try (
                InputStream is1 = socket.getInputStream();
                OutputStream os1 = socket.getOutputStream();
        ) {
            is = is1;
            os = os1;
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);

            if (getRequest())
                processRequest();

            sendMessage();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (log) System.out.println(threadName + " finished.");
    }

    private Account getAccount(String paramName) {
        String paramValue = params.get(paramName);
        if (paramValue.isEmpty())
            throw new IllegalArgumentException("'" + paramName + "' is not set");
        return ss.get(paramValue);
    }

    private double getDouble(String paramName) {
        String paramValue = params.get(paramName);
        if (paramValue.isEmpty())
            throw new IllegalArgumentException("'" + paramName + "' is not set");
        try {
            return Double.valueOf(paramValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + paramName + "' is not a valid Double value");
        }
    }

    private boolean processRequest() {
        String answer = "ok";
        Account acc;
        double value;
        try {
            switch (method) {
                case "balance": // Получение информации по балансу // public double balance(Account account)
                    acc = getAccount("id");
                    value = as.balance(acc);
                    answer = String.valueOf(value);
                    break;
                case "deposit": // Пополнение счета // public void deposit(Account account, double amount)
                    acc = getAccount("id");
                    value = getDouble("amount");
                    as.deposit(acc, value);
                    break;
                case "withdraw": // Снятие наличных // public void withdraw(Account account, double amount)
                    acc = getAccount("id");
                    value = getDouble("amount");
                    as.withdraw(acc, value);
                    break;
                case "transfer": // Перевод на другую карту // public void transfer(Account from, Account to, double amount)
                    acc = getAccount("fromId");
                    Account accTo = getAccount("toId");
                    value = getDouble("amount");
                    as.transfer(acc, accTo, value);
                    break;
                default:
                    throw new IllegalArgumentException("Method '" + method + "' not supported");
            }
            sendText = responceOk.replace("{length}", String.valueOf(answer.length())).replace("{text}", answer);
            System.out.println(threadName + " AtmHttpServer.RequestHandler.Method: "+method+" success. Answer="+answer);
            return true;
        } catch (Exception e) {
            sendText = responceError.replace("{text}", e.getMessage());
            System.out.println(threadName + " AtmHttpServer.RequestHandler.Method: "+method+". Error="+e.getMessage());
            return false;
        }
    }

    private boolean getRequest() throws IOException {
        if (log) System.out.println(threadName + " AtmHttpServer.RequestHandler.processRequest()");
            Scanner sc = new Scanner(is);
            boolean done = false;
            boolean isGetGot = false;
            boolean isHostGot = false;
            while (!done && sc.hasNextLine()) {
                String str = sc.nextLine();
                //System.out.println(threadName + " " + str);
                if (str.equals("")) {
                    done = true;
                } else {
                    if(str.equals("GET /favicon.ico HTTP/1.1")) {
                        return false;
                    } else if (str.startsWith(getPrefix)) {
                        if (isGetGot) {
                            sendText = responceError.replace("{text}", "Double GET prefix");
                            return false;
                        }
                        isGetGot = true;
                        getRequestGet(str.substring(getPrefix.length()));
                    } else if (str.startsWith(hostPrefix)) {
                        if (isHostGot) {
                            sendText = responceError.replace("{text}", "Double HOST prefix");
                            return false;
                        }
                        isHostGot = true;
                        getRequestHost(str.substring(hostPrefix.length()));
                    }
                }
            }
            if (!isGetGot) {
                sendText = responceError.replace("{text}", "No GET prefix found");
                return false;
            }
            return true;
    }

    private void getRequestHost(String line) {
        //line=localhost
        host = line;
    }

    private void getRequestGet(String line) {
        //line=resource?param1=value1&param2=value2 HTTP/1.1
        int idxQ = line.indexOf('?');
        if (idxQ >= 0) {
            int idxS = line.indexOf(' ', idxQ);
            method = line.substring(0, idxQ);
            String[] paramStrings = (idxS > 0 ? line.substring(idxQ + 1, idxS) : line.substring(idxQ + 1))
                    .split("\\&");
            for (String str : paramStrings) {
                int idxE = str.indexOf('=');
                params.put(str.substring(0, idxE), str.substring(idxE + 1));
            }
        } else {
            method = line;
        }
    }

    private void sendMessage() throws IOException {
        if (log) System.out.println(threadName + " AtmHttpServer.RequestHandler.sendMessage(text)");
            os.write(sendText.getBytes());
    }

}
