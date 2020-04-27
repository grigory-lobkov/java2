package ru.progwards.java2.lessons.http.bankomat.service.impl;

import ru.progwards.java2.lessons.http.bankomat.Store;
import ru.progwards.java2.lessons.http.bankomat.model.Account;
import ru.progwards.java2.lessons.http.bankomat.service.AccountService;
import ru.progwards.java2.lessons.http.bankomat.service.StoreService;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class AtmClient implements AccountService {

    private static final int SOCKET_TIMEOUT = 1000;
    String host;
    int port;
    static final String httpPrefix = "HTTP/";
    static final String requestString = "GET /{method} HTTP/1.1\nhostname: localhost\n\n";
    static final String requestBalance = requestString.replace("{method}", "balance?id={id}");
    static final String requestDeposit = requestString.replace("{method}", "deposit?id={id}&amount={amount}");
    static final String requestWithdraw = requestString.replace("{method}", "withdraw?id={id}&amount={amount}");
    static final String requestTransfer = requestString.replace("{method}", "transfer?fromId={fromId}&toId={toId}&amount={amount}");

    public AtmClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public double balance(Account account) {
        if(account==null)
            throw new NullPointerException("account must be set");
        String id = account.getId();
        if(id.isEmpty())
            throw new NullPointerException("account.id must be set");

        String request = requestBalance
                .replace("{id}", id);

        String answer = doRequest(request);

        return Double.valueOf(answer);
    }

    private String doRequest(String request) {
        String code = "";
        boolean nextIsData = false;

        try (
                Socket client = new Socket(host, port);
                PrintWriter pw = new PrintWriter(client.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        ) {
            client.setSoTimeout(SOCKET_TIMEOUT);

            pw.println(request);
            pw.flush();

            String line;
            while ((line = br.readLine()) != null) {
                if (nextIsData) {
                    if(!code.equals("200"))
                        throw new RuntimeException(line);
                    return line;
                } else if (code.isEmpty() && line.startsWith(httpPrefix)) {
                    String[] s = line.split(" ");
                    if (s.length > 1) {
                        code = s[1];
                        continue;
                    } else {
                        throw new RuntimeException("HTTP header is bad");
                    }
                } else if (line.isEmpty() && !code.isEmpty()) {
                    nextIsData = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deposit(Account account, double amount) {
        if(account==null)
            throw new NullPointerException("account must be set");
        String id = account.getId();
        if(id.isEmpty())
            throw new NullPointerException("account.id must be set");

        String request = requestDeposit
                .replace("{id}", id)
                .replace("{amount}", String.valueOf(amount));

        doRequest(request);
    }

    @Override
    public void withdraw(Account account, double amount) {
        if(account==null)
            throw new NullPointerException("account must be set");
        String id = account.getId();
        if(id.isEmpty())
            throw new NullPointerException("account.id must be set");

        String request = requestWithdraw
                .replace("{id}", id)
                .replace("{amount}", String.valueOf(amount));

        doRequest(request);
    }

    @Override
    public void transfer(Account from, Account to, double amount) {
        if(from==null)
            throw new NullPointerException("'from' must be set");
        String fromId = from.getId();
        if(fromId.isEmpty())
            throw new NullPointerException("'from.id' must be set");
        if(to==null)
            throw new NullPointerException("'to' must be set");
        String toId = to.getId();
        if(toId.isEmpty())
            throw new NullPointerException("'to.id' must be set");

        String request = requestTransfer
                .replace("{fromId}", fromId)
                .replace("{toId}", toId)
                .replace("{amount}", String.valueOf(amount));

        doRequest(request);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String id = "60f1a5cc-97f8-436c-891d-048b76a7c920";
        String id2 = "46a87cf4-439a-48fc-a012-dbfbd8d1aa9b";
        // was:
        //{"id":"60f1a5cc-97f8-436c-891d-048b76a7c920","holder":"Account_8","date":"May 3, 2020, 3:24:50 PM","amount":221751.524022907,"pin":1008} +10_000-30_000-50_000 = -70_000
        //{"id":"46a87cf4-439a-48fc-a012-dbfbd8d1aa9b","holder":"Account_1","date":"May 3, 2020, 3:24:50 PM","amount":323595.0683072427,"pin":1001} +50_000
        // result:
        //{"id":"60f1a5cc-97f8-436c-891d-048b76a7c920","holder":"Account_8","date":"May 3, 2020, 3:24:50 PM","amount":151751.524022907,"pin":1008}
        //{"id":"46a87cf4-439a-48fc-a012-dbfbd8d1aa9b","holder":"Account_1","date":"May 3, 2020, 3:24:50 PM","amount":373595.0683072427,"pin":1001}
        Account acc = new Account();
        acc.setId(id);
        Account acc2 = new Account();
        acc2.setId(id2);
        double amount = 10_000;

        Thread.sleep(1000);
        AccountService as = new AtmClient("localhost", 80);

        System.out.println(as.balance(acc));
        as.deposit(acc, amount);
        as.withdraw(acc, amount*3);
        as.transfer(acc, acc2, amount*5);
    }
}
