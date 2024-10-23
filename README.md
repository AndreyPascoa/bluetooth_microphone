<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
    <h1>Bluetooth Microphone</h1>
    <p>Este projeto é uma aplicação Android que permite gravar áudio utilizando um fone de ouvido Bluetooth e salvar o arquivo gravado no armazenamento do dispositivo, utilizando o <code>MediaStore</code> para compatibilidade com o Android 10 e superior. A gravação de áudio é feita via <code>MediaRecorder</code>, e o arquivo é salvo na pasta <code>Music/Recordings</code>.</p>
    <h2>Funcionalidades</h2>
    <ul>
        <li>Conecta-se a um dispositivo Bluetooth específico (F9-5C, no exemplo).</li>
        <li>Inicia e para a gravação de áudio via Bluetooth, utilizando <code>MediaRecorder</code>.</li>
        <li>Salva a gravação de áudio no armazenamento externo na pasta <code>Music/Recordings</code>.</li>
        <li>Utiliza o <code>MediaStore</code> para gerenciar o acesso ao arquivo e evitar permissões obsoletas.</li>
    </ul>
    <h2>Estrutura do Código</h2>
    <h3>1. Permissões Necessárias</h3>
    <p>A aplicação solicita as permissões necessárias para conectar-se ao Bluetooth e gravar áudio. Essas permissões são gerenciadas no código:</p>
    <ul>
        <li><strong>BLUETOOTH_CONNECT</strong>: Usada para conectar e gerenciar dispositivos Bluetooth.</li>
        <li><strong>RECORD_AUDIO</strong>: Usada para capturar o áudio via fone de ouvido Bluetooth.</li>
    </ul>
    <h3>2. Processo Bluetooth</h3>
    <p>O código verifica os dispositivos Bluetooth emparelhados, busca pelo dispositivo alvo (neste caso, o fone "F9-5C") e tenta conectar-se ao fone para gravar áudio:</p>
    <pre><code>if (device.getName().equals(TARGET_DEVICE_NAME)) {
    Log.d("BluetoothSCO", "Fone de ouvido Bluetooth encontrado: " + device.getName());
    startBluetoothSco();
}
</code></pre>
    <h3>3. Gravação de Áudio com <code>MediaRecorder</code></h3>
    <p>Ao encontrar o fone de ouvido correto, o <code>MediaRecorder</code> é configurado para capturar o áudio. Utilizamos o <code>MediaStore</code> para criar e salvar o arquivo no caminho correto, sem a necessidade de permissões de armazenamento de escrita que foram descontinuadas:</p>
    <pre><code>Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
mediaRecorder.setOutputFile(getContentResolver().openFileDescriptor(audioUri, "w").getFileDescriptor());
</code></pre>
    <p>A gravação começa e o áudio é salvo na pasta <code>Music/Recordings</code> no formato <strong>3GP</strong>.</p>
    <h3>4. Salvamento e Localização do Arquivo</h3>
    <p>O arquivo de áudio é salvo automaticamente no diretório:</p>
    <pre><code>Music/Recordings/recorded_audio.3gp</code></pre>
    <p>Esse caminho pode ser alterado conforme a necessidade, definindo os valores no <code>ContentValues</code>.</p>
    <h3>5. Parada da Gravação</h3>
    <p>Quando o ciclo de vida da atividade é encerrado, a gravação é parada e o recurso SCO é desativado:</p>
    <pre><code>mediaRecorder.stop();
audioManager.stopBluetoothSco();
</code></pre>
    <h2>Como Rodar o Projeto</h2>
    <ol>
        <li><strong>Clone o Repositório:</strong>
            <pre><code>git clone &lt;url_do_repositorio&gt;</code></pre>
        </li>
        <li><strong>Abra o Projeto no Android Studio.</strong></li>
        <li><strong>Compile e Execute:</strong>
            <ul>
                <li>Conecte um dispositivo Android ou utilize um emulador.</li>
                <li>Execute o aplicativo diretamente pelo Android Studio.</li>
            </ul>
        </li>
        <li><strong>Teste com seu Fone de Ouvido Bluetooth:</strong>
            <ul>
                <li>Certifique-se de que o fone Bluetooth está emparelhado e conectado ao dispositivo.</li>
                <li>Use os botões na interface para iniciar e parar a gravação.</li>
            </ul>
        </li>
    </ol>
    <h2>Requisitos</h2>
    <ul>
        <li><strong>Android Studio</strong> 4.0 ou superior.</li>
        <li><strong>SDK Android 10 (API 29)</strong> ou superior.</li>
        <li><strong>Permissões no <code>AndroidManifest.xml</code>:</strong>
            <pre><code>&lt;uses-permission android:name="android.permission.BLUETOOTH_CONNECT" /&gt;
&lt;uses-permission android:name="android.permission.RECORD_AUDIO" /&gt;
</code></pre>
        </li>
    </ul>
    <h2>Melhorias Futuras</h2>
    <ul>
        <li>Suporte a múltiplos dispositivos Bluetooth.</li>
        <li>Interface de usuário aprimorada para feedback de gravação.</li>
        <li>Adicionar opção de visualizar e reproduzir as gravações diretamente no aplicativo.</li>
    </ul>
</body>
</html>
