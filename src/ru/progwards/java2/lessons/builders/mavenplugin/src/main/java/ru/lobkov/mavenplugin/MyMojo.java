package ru.lobkov.mavenplugin;

/*

У меня почему-то нет такого архитипа. Пришлось выкручиваться:

C:\Users\Grigory\IdeaProjects>set JAVA_HOME=C:\Program Files\Java\jre1.8.0_241

C:\Users\Grigory\IdeaProjects>"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2018.3.5\plugins\maven\lib\maven3\bin\mvn" archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-plugin -DarchetypeVersion=1.4

[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] >>> maven-archetype-plugin:3.1.2:generate (default-cli) > generate-sources @ standalone-pom >>>
[INFO]
[INFO] <<< maven-archetype-plugin:3.1.2:generate (default-cli) < generate-sources @ standalone-pom <<<
[INFO]
[INFO] --- maven-archetype-plugin:3.1.2:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] Archetype repository not defined. Using the one from [org.apache.maven.archetypes:maven-archetype-plugin:1.4] found in catalog remote
Define value for property 'groupId': ru.lobkov.mavenplugin
Define value for property 'artifactId': mavenplugin
Define value for property 'version' 1.0-SNAPSHOT: :
Define value for property 'package' ru.lobkov.mavenplugin: :
Confirm properties configuration:
groupId: ru.lobkov.mavenplugin
artifactId: mavenplugin
version: 1.0-SNAPSHOT
package: ru.lobkov.mavenplugin
 Y: : Y
[INFO] ----------------------------------------------------------------------------
[INFO] Using following parameters for creating project from Archetype: maven-archetype-plugin:1.4
[INFO] ----------------------------------------------------------------------------
[INFO] Parameter: groupId, Value: ru.lobkov.mavenplugin
[INFO] Parameter: artifactId, Value: mavenplugin
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: package, Value: ru.lobkov.mavenplugin
[INFO] Parameter: packageInPathFormat, Value: ru/lobkov/mavenplugin
[INFO] Parameter: package, Value: ru.lobkov.mavenplugin
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: groupId, Value: ru.lobkov.mavenplugin
[INFO] Parameter: artifactId, Value: mavenplugin
[INFO] Project created from Archetype in dir: C:\Users\Grigory\IdeaProjects\mavenplugin
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 58.046 s
[INFO] Finished at: 2020-05-23T11:03:57+05:00
[INFO] Final Memory: 14M/219M
[INFO] ------------------------------------------------------------------------

C:\Users\Grigory\IdeaProjects>

*/

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Goal which touches a timestamp file.
 */
@Mojo( name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class MyMojo
    extends AbstractMojo {
    /**
     * Location of the package
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * Location of the package
     */
    @Parameter(defaultValue = "${project.build.finalName}", property = "packageName", required = true)
    private String pkgName;

    /**
     * Extension of package file
     */
    @Parameter(defaultValue = "${project.packaging}", property = "packageExt", required = true)
    private String pkgExt;

    /**
     * GAV
     */
    @Parameter(defaultValue = "${project.groupId}", property = "groupId", required = true)
    private String groupId;

    @Parameter(defaultValue = "${project.artifactId}", property = "artifactId", required = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.version}", property = "version", required = true)
    private String version;

    /**
     * Email, where to send the report
     */
    @Parameter(property = "emailTo")
    private String emailTo;

    /**
     * от кого письмо
     */
    @Parameter(defaultValue = "grigorymail@mail.ru", property = "emailFrom", required = true)
    private String emailFrom;
    /**
     * тема сообщения
     */
    @Parameter(defaultValue = "Project ${project.artifactId} compiled", property = "subject", required = true)
    private String subject;
    /**
     * имя для авторизации на исходящем почтовом сервере
     */
    @Parameter(defaultValue = "smtp.mail.ru", property = "authServ", required = true)
    private String authServ;
    /**
     * имя для авторизации на исходящем почтовом сервере
     */
    @Parameter(defaultValue = "grigorymail@mail.ru", property = "authUser", required = true)
    private String authUser;
    /**
     * пароль для авторизации
     */
    @Parameter(defaultValue = "********", property = "authPass", required = true)
    private String authPass;

    private final String settingsFileName = "email.settings";

    public void execute() throws MojoExecutionException {
        File f = outputDirectory;
        if (!f.exists())
            f.mkdirs();

        File cfgFile = new File(f, settingsFileName);
        String fileCfg = getFileEmail(cfgFile);
        if (fileCfg != null && !fileCfg.isEmpty())
            emailTo = fileCfg;

        if (emailTo == null || emailTo.isEmpty())
            throw new MojoExecutionException("'email' parameter is not set");
        File pkgFile = new File(f, pkgName + '.' + pkgExt);
        sendMail("stb.cam@mail.ru", pkgFile);
    }

    private String getFileEmail(File cfgFile) {
        if (cfgFile.exists() && cfgFile.canRead() && !cfgFile.isDirectory())
            try (
                    FileReader fr = new FileReader(cfgFile);
                    BufferedReader br = new BufferedReader(fr)
            ) {
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    public void sendMail(String toEmail, File pkgFile) {
        Properties props = new Properties();
        props.put("mail.smtp.host", authServ);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Session session = Session.getDefaultInstance(props);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            String result = "Email sent successfully, ";

            String html = "<html><head><style type='text/css'>" +
                    "table,th,td{border:1px solid gray;border-collapse:collapse}" +
                    "th,td{padding:0.3em}" +
                    "</style></head>" +
                    "<body><h1>" + message.getSubject() + "</h1>" +
                    "<table border=1>" +
                    "<tr><td>groupId</td><td>" + groupId + "</td></tr>" +
                    "<tr><td>artifactId</td><td>" + artifactId + "</td></tr>" +
                    "<tr><td>version</td><td>" + version + "</td></tr>" +
                    "</table><p>verified.</p>" +
                    "<p>" + pkgFile.getAbsolutePath() + " attached.</p>" +
                    "<small>This messege was sent by my test mavenplugin</small>" +
                    "</body></html>";

            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(html, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            if (pkgFile.exists() && pkgFile.canRead() && !pkgFile.isDirectory()) {
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.attachFile(pkgFile);
                multipart.addBodyPart(messageBodyPart);
                result += "package attached.";
            } else {
                result += "package was not attached - not found.";
            }

            message.setContent(multipart);

            Transport.send(message, authUser, authPass);

            System.out.println(result);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}