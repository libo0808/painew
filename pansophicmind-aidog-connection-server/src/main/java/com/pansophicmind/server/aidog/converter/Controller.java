package com.pansophicmind.server.aidog.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/test")
public class Controller {

    @GetMapping(value = "/opusConverterWav")
    public void test() throws Exception {
        byte[] opusAudio1 = new byte[]{
                88, 14, -45, -112, 103, -69, -67, 127, 80, -67, -2, 42, 59, 52, 124, 81, 38, -121, 80, -83, -75, -124, 82, 0, 87, -4, -90, 67, 115, 9, -84, 49, -3, -39, -7, 91, -16, -81, -50, -13, 74, 13, -100, -2, 97, -9, -4, -113, 3, 113, -5, -105, -12, -93, 107, 28, -28, -104, 19, 96, -97, 105, -3, 121, -67, -3, 119, -32, -87, 38, 1, -7, 22, -27, -17
        };
        byte[] opusAudio2 = new byte[]{
                88, 14, -45, -122, 4, -126, -66, -88, -18, 109, -14, 44, 97, 109, -78, 122, -18, 80, -63, 77, -42, 2, -59, -120, -18, 21, 113, -97, 4, 62, 20, 32, 106, -62, 0, -8, 88, -56, -1, 119, -114, 9, -30, -74, -18, -26, 6, -120, -91, -112, -100, -87, 53, -62, -118, 66, -60, 117, 31, 61, 91, -73, -69, -56, 46, -59, 56, 59, -90, -26, -17, -82, 59, -29, -58,-95, -72
        };
//        try {
//            byte[] wavData = OpusConverterWav.convert(opusAudio, 16000, 1);
//            log.error(Arrays.toString(wavData));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        OpusConverterWav2.convert(opusAudio1, "/pai/server/temp/opusAudio1.wav");
//        OpusConverterWav2.convert(opusAudio2, "/pai/server/temp/opusAudio2.wav");
    }

}
