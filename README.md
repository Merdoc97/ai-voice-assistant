# Voice AI Assistant

## Overview
Voice AI Assistant is a desktop application that combines speech recognition with AI-powered responses. It functions as a voice-based AI assistant that:

1. Records audio from your microphone
2. Transcribes the speech to text using OpenAI's audio transcription model
3. Sends the transcribed text as a prompt to OpenAI's chat model
4. Displays both the transcribed question and the AI's response in the UI

## Features
- Simple JavaFX-based user interface
- Audio input device selection
- Real-time audio recording
- Speech-to-text transcription using OpenAI models
- AI-powered responses to your questions
- Configurable audio format and output settings
- Support for audio capture from headphones output using audio mixer configurations

## Requirements
- Java 21 or higher
- Maven
- OpenAI API key

## Installation and Setup
1. Clone the repository
2. Configure your OpenAI API key in `src/main/resources/application.yml`:
   ```yaml
   spring:
     ai:
       openai:
         api-key: "your-api-key-here"
   ```
3. Build the application:
   ```
   mvn clean package
   ```
4. Run the application:

   **Standard method:**
   ```
   java -jar target/ttsagent-0.0.1-SNAPSHOT.jar
   ```
   > **Note:** Despite the JAR filename containing "ttsagent", this is a Voice AI Assistant application.

   **With JavaFX modules (recommended for some environments):**
   ```
   java -Dfile.encoding=UTF-8 -Dspring.config.location=application.yml --module-path "pathToJavaFxSDK\lib" --add-modules javafx.controls,javafx.fxml -jar ttsagent-0.0.1-SNAPSHOT.jar
   ```

   > **Note:** Replace `pathToJavaFxSDK` with the actual path to your JavaFX SDK installation directory. This method is necessary if you're running the application in an environment where JavaFX modules are not included in the default Java runtime.

## Usage
1. Launch the application
2. Select your audio input device from the dropdown menu
3. Click the START button to begin recording
4. Speak your question or prompt
5. Click the STOP button to end recording and process your speech
6. View the transcribed text and AI response in the text area
7. Use the CLEAR button to clear the output area

## Configuration Properties
The application can be configured through the `application.yml` file:

### OpenAI Configuration
```yaml
spring:
  ai:
    openai:
      api-key: "your-api-key-here"
      chat:
        options:
          stream-usage: true
          model: gpt-4o-mini
          max-tokens: 20000
          temperature: 0.1
      audio:
        speech:
          options:
            model: "gpt-4o-mini-transcribe"
```

### AI Prompt Configuration
```yaml
ai:
  promtText: "<question>"
```

The `ai.promtText` property defines the template used for prompts sent to the OpenAI chat model. The template can include the `<question>` placeholder, which will be replaced with the transcribed text from the audio input. You can customize this template to provide additional context or instructions to the AI model. If not specified, it defaults to simply using the transcribed text as the prompt.

Depending on how you customize the prompt, the Voice AI Assistant can function in various roles:

- **Video/Audio Translator**: Configure the prompt to translate content from videos or audio recordings
- **Voice Helper**: Set up the prompt to provide helpful responses to general questions
- **Stenographer**: Create a prompt that focuses on accurate transcription of spoken content from headphones
- **Interview Assistant**: Design a prompt to help answer interview questions with professional responses
- **Meeting Transcriber**: Configure the prompt to transcribe and summarize audio meetings
- **Language Translator**: Set up the prompt to translate spoken content into different languages
- **Bilingual Assistant**: Create a prompt that responds in two languages simultaneously

The flexibility of the prompt configuration, combined with the ability to capture audio from any input source (including headphones via audio mixer), opens up countless possibilities for how you can use this application. By customizing the `ai.promtText` property, you can transform the Voice AI Assistant to serve almost any audio-processing and AI-response need.

### Audio Format Configuration
```yaml
audio:
  format:
    channels: 1
    sample-size-bit: 16
    sample-rate: 44100
    signed: true
    big-endian: false
```

### File Output Configuration
```yaml
file:
  output:
    output-dir: /tmp/voice-ai
```

## Project Structure
- `TtsAgentApplication.java`: Main application entry point (despite the name, this is a Voice AI application)
- `MainController.java`: UI controller for the JavaFX interface
- `TTSServiceImpl.java`: Service for handling speech recognition and AI responses
- `VoiceHandlerImpl.java`: Handles audio recording and processing
- Configuration classes:
  - `AudioFormatConfig.java`: Audio format settings
  - `FileOutputConfig.java`: File output directory settings

## User Interface

### UI Layout
The application has a simple and intuitive user interface consisting of:

```
┌─────────────────────── Voice AI Assistant ─────────────────────────┐
│                                                                     │
│  ┌─────────────────┐ ┌──────┐ ┌──────┐ ┌───────┐                    │
│  │ Microphone 1  ▼ │ │START │ │STOP  │ │CLEAR  │                    │
│  └─────────────────┘ └──────┘ └──────┘ └───────┘                    │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                                                             │    │
│  │ processing...                                               │    │
│  │ -----------------------------                               │    │
│  │ The question is: What's the weather like today?             │    │
│  │ The answer is:                                              │    │
│  │ The current weather depends on your location. To provide    │    │
│  │ an accurate forecast, I would need to know where you are.   │    │
│  │ However, you can check the current weather by looking up    │    │
│  │ your city on weather services like Weather.com or           │    │
│  │ AccuWeather, or by using a weather app on your device.      │    │
│  │                                                             │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### UI Components

1. **Audio Input Device Dropdown**
   - Located at the top-left of the window
   - Displays all available audio input devices (microphones) on your system
   - You must select a device before recording can begin

2. **Control Buttons**
   - **START** (Red): Begins the audio recording process
     - Changes to "RECORDING..." when active
     - Displays "processing..." in the output area
   - **STOP** (Blue): Stops the recording and processes the audio
     - Triggers the transcription and AI response generation
   - **CLEAR** (Gray): Clears all text from the output area

3. **Text Output Area**
   - Displays:
     - Status messages (e.g., "processing...")
     - The transcribed text from your speech (prefixed with "The question is:")
     - The AI's response (prefixed with "The answer is:")

### How to Use the UI

1. **Select an Audio Input Device**:
   - Click the dropdown menu in the top-left corner
   - Select your preferred microphone from the list
   - If no device is selected, you'll see an error message when trying to record

2. **Start Recording**:
   - Click the red START button
   - The button text will change to "RECORDING..."
   - Speak clearly into your selected microphone
   - A "processing..." message will appear in the output area

3. **Stop Recording and Process**:
   - Click the blue STOP button when you've finished speaking
   - The application will:
     - Stop recording
     - Transcribe your speech to text
     - Send the text to the AI model
     - Display both your question and the AI's response in the output area

4. **View Results**:
   - The output area will show:
     - A separator line ("---------------------")
     - Your transcribed question ("The question is: [your speech]")
     - The AI's response ("The answer is: [AI response]")

5. **Clear the Output**:
   - Click the gray CLEAR button to reset the output area
   - This is useful when you want to start a new conversation

## Dependencies
- Spring Boot 3.4.4
- Spring AI 1.0.0
- JavaFX 21
- Lombok
