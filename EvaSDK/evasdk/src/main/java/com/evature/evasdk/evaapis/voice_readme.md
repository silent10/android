Evature Voice Recognition
--------------------------


Classes in this folder have confusing names and it is hard to tell how one is dependent on an another,
so to make it clearer here is a short summary, from top level to the lower levels:


  Activate voice recognition - without GUI
  this is the responsibility of the class "EvaVoiceComponent"
  

  SoundLevelView -
  The application can use the "SoundLevelView" class if it wants to integrate the "wavy recording volume" lines visualization in its.


The rest of the classes are lower level for internal use only.


1. SpeechAudioStreamer -
this class listens to the microphone in a different thread, its output is an encoded stream of audio.
The class uses VAD algorithm to detect silence and stop the recording.
The actual encoding is done in the class FLACStreamEncoder - which is actually a bridge to the C++ Flac library.

2. FLACStreamEncoder - see above - encodes a stream of PCM audio into FLAC audio (in its own thread).

3. EvaVoiceClient - this class is responsible to upload a stream using chunked HTTP POST.
4. EvaTextClient - responsible for sending Eva requests via text (eg. when undo'ing or editing last utterance).
5. EvaSpeak - responsible for TTS (Text to Speech) initalizing, speech queue and speech callbacks.
6. EvaLocationUpdater - responsible for updating location from GPS or other location provider.
7. DebugStream - this is a class that wraps the encoding stream -
it helped debug the process, and also it adds profiling data such as when the streaming actually started and when it actually finished. 
These timestamps are hidden when the wiring goes directly from the Encoder to the Uploader - both are 3rd party code (FLAC lib and Android platform). 


  

