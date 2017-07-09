#ifndef VAD_WEBRTC_H_
#define VAD_WEBRTC_H_

#include "webrtc/common_audio/vad/include/vad.h"


class VADWebRTC {
 public:
// Sets the VAD operating mode. A more aggressive (higher mode) VAD is more
// restrictive in reporting speech. Put in other words the probability of being
// speech when the VAD returns 1 is increased with increasing mode. As a
// consequence also the missed detection rate goes up.
   enum Aggressiveness {
     kVadNormal = 0,
     kVadLowBitrate = 1,
     kVadAggressive = 2,
     kVadVeryAggressive = 3
   };

  enum VoiceActivity { kPassive = 0, kActive = 1, kError = -1 };

  VADWebRTC(Aggressiveness aggressiveness);
  ~VADWebRTC();

  // Calculates a VAD decision for the given audio frame. Valid sample rates
  // are 8000, 16000, and 32000 Hz; the number of samples must be such that the
  // frame is 10, 20, or 30 ms long.
  VoiceActivity voiceActivity(const int16_t* audio,
                                 size_t num_samples,
                                 int sample_rate_hz);

  // Resets VAD state.
  virtual void reset();

  private:
   VadInst* handle_;
   Aggressiveness aggressiveness_;
};



#endif  // VAD_WEBRTC_H_
