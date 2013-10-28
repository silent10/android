Evature Voice Recognition
--------------------------


Classes in this folder have confusing names and it is hard to tell how one is dependent on an another,
so to make it clearer here is a short summary, from top level to the lower levels:


There are two possible entry points to the voice-recognition

1. Activate an activity (with GUI) -
  this will open a screen showing default GUI of recording (ie. "stop" button and wavy green lines that visualize the recording)
  this is the responsibility of the class "EvaSpeechRecognitionActivity".

2. Activate voice recognition - without GUI
  this is the responsibility of the class "green"
  
  So EvaSpeechRecognitionActivity  uses  EvaVoiceComponent
  In addition, the "wavy green lines" logic is in "SoundLevelView" class, so the activity uses that as well.
  
  
The application can use either "EvaVoiceComponent"  or  "EvaSpeechRecognitionActivity" to initiate recording (depending if it wants the default GUI or no GUI).

3. SoundLevelView - 
The application can use the "SoundLevelView" class if it wants to integrate the green lines visualization in its.


The rest of the classes are lower level for internal use only.


4. SpeechAudioStreamer -  
this class listens to the microphone in a different thread, its output is an encoded stream of audio.
The class uses VAD algorithm to detect silence and stop the recording.
The actual encoding is done in the class FLACStreamEncoder - which is actually a bridge to the C++ Flac library.

5. FLACStreamEncoder - see above - encodes a stream of PCM audio into FLAC audio.

6. EvaVoiceClient - this class is responsible to upload a stream using chunked HTTP POST.
  
7. DebugStream - this is a class that wraps the encoding stream -
it helped debug the process, and also it adds profiling data such as when the streaming actually started and when it actually finished. 
These timestamps are hidden when the wiring goes directly from the Encoder to the Uploader - both are 3rd party code (FLAC lib and Android platform). 


  

