Voraussetzungen
<ol class="big">
 	<li>Eine voll funktionsfähige Keycloak Installation.</li>
 	<li>Die Extension als .jar-Datei (<a href="">Download hier</a>)</li>
</ol>

<strong>Installation</strong>
<ol class="big" >
 	<li>Beenden Sie den Keycloak-Server</li>
        <li>Kopieren Sie die .jar-Datei ("secsign-authenticator.jar") in den Unterordner "providers" des Keycloak-Ordners</li>
 	<li>Bauen Sie die Installation neu auf mit "kc.sh build"
            Dies löscht keine vorhandenen Einstellungen. Es integriert lediglich neu gefundene Extensions ins System
        </li>
        <li>Anschließen starten Sie den Server (z.B. mit "kc.sh start" oder "kc.sh start-dev")</li>
        <li>Loggen Sie sich als Admin in der Keycloak Admin Console ein.</li>
        <li>Wählen Sie das Realm, welches durch 2FA gesichert werden soll</li>
        <li>Öffnen Sie die "Authentication" Einstellungen</li>
        <li>Wenn Sie bereits einen eigenen Flow erstellt haben, wählen Sie diesen. Ansonsten kopieren Sie einen existierenden Flow oder erstellen Sie einen neuen.</li>
        <li>In der Ausführungsreihenfolge muss vor dem SecSign ID Authenticator ein Schritt vorkommen, welcher den User identifiziert (z.B Username Password Form). In einem neuen Flow können Sie diesen hinzufügen, indem Sie "Add execution" wählen</li>
        <li>Anschließend fügen Sie ebenfalls durch "Add execution" den "SecSign ID" Authenticator hinzu.</li>
        <li>Wenn Sie diesen hinzugefügt haben, ändern Sie das eingestellte "Requirement" auf "Required", da ansonsten die 2FA umgangen werden kann und für Benutzer ohne gespeicherte SecSign ID automatisch übersprungen wird.</li>

<img style="width:100%;" src="https://www.secsign.com/wp-content/uploads/2022/07/flow.png" alt="Flow mit SecSign ID Authenticator" />

        <li>Wenn Sie einen on-premise SecSign ID Server besitzen, wählen Sie auf der rechten Seite unter "Actions" die Option "Config" und befolgen Sie die Hilfestellungen unter Konfiguration.</li>
        <li>Um den erstellten Flow nun zu nutzen wählen Sie den Tab "Bindings" und wählen Sie den neuen Flow als "Browser Flow" (Für Login im Browser) aus.</li>
        
        <img style="width:100%;" src="https://www.secsign.com/wp-content/uploads/2022/07/bindings.png" alt="Auswahl des passenden Flows" />

</ol>
[header rank="1" short="Konfiguration"]Konfiguration des Plugins [/header]

Es gibt verschiedene Konfigurationsmöglichkeiten:
<ol class="big">
 	<li>Im erstellten Flow können Sie rechts unter "Actions" die Option "Config" wählen, um so ihren on-premise SecSign ID-Server einzurichten. Hierfür benötigen Sie 3 Einstellungen:</li>
<img style="width:100%;" src="https://www.secsign.com/wp-content/uploads/2022/07/config.png" alt="Config des Secsign Authenticators" />
            <ul>
              <li><strong>SecSign ID Server URL:</strong> Die URL ihres on-premise SecSign ID Server. (z.B: https://idserver.yourcompany.com)</li>
              <li><strong>Pin Account User:</strong> Der PinAccount, um auf Ihren Server zuzugreifen. Dieser benötigt die Rechte, um Secsign IDs auf dem Server zu erstellen</li>
	      <li><strong>Pin Account Password:</strong> Das Passwort des PinAccount, um auf Ihren Server zuzugreifen.</li>
            </ul>

 	<li>Außerdem können Sie die SecSign IDs von einzelnen Usern ändern oder manuell hinzufügen. Dazu navigieren Sie in der Admin-Konsole zu "Users" und wählen den User, den 
          Sie anpassen wollen. Im Tab "Attributes" können Sie nun, wenn vorhanden, das Attribut "secsignid" anpassen oder neu hinzufügen.
        </li>
<img style="width:100%;" src="https://www.secsign.com/wp-content/uploads/2022/07/attributes.png" alt="Config des Secsign Authenticators" />
</ol>

[header rank="1" short="Ablauf"]Ablauf des Logins [/header]
<ul>
<li>Der Nutzer identifiziert sich im ersten Schritt, z.B. über eine Username Password Form.</li>
<li>Anschließend wird geprüft, ob der Benutzer bereits eine SecSign ID hat.</li>
<li>Wenn er eine SecSign ID gespeichert hat, so wird die Authentifzierung automatisch damit gestartet.</li>

<li>Wenn er keine SecSign ID hat, so wird nun eine für ihn erstellt und der entsprechende QR-Code für diese SecSign ID auf dem Bildschirm angezeigt.
Diese muss der User nun mit der App auf dem Smartphone scannen. Wenn dies durchgeführt wurde, beginnt automatisch die Authentifizierung mit dieser SecSign ID.
</li>

<li>Beim nächsten Login kann der User dann direkt die Secsign ID zum Authentifizieren nutzen.</li>
</ul>
<img style="max-width:200px;vertical-align: top;" src="https://www.secsign.com/wp-content/uploads/2022/07/login-form.png" alt="Username und Password Login Form" /> <img style="max-width:200px;vertical-align: top;" src="https://www.secsign.com/wp-content/uploads/2022/07/qr.png" alt="QR-Code zur Erstellung der SecSign ID" /> <img style="max-width:200px;vertical-align: top;" src="https://www.secsign.com/wp-content/uploads/2022/07/auth.png" alt="Auth-Prozess mit der SecSign ID" />


