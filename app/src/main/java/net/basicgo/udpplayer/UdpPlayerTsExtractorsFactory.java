package net.basicgo.udpplayer;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;


public final class UdpPlayerTsExtractorsFactory implements  ExtractorsFactory {
    @Override
    public synchronized Extractor[] createExtractors() {
        Extractor[] extractors = new Extractor[1];

        extractors[0] = new TsExtractor();

        return extractors;
    }
}
