package org.coreocto.dev.hf.clientlib.parser;

import java.io.File;
import java.util.List;

public interface IFileParser {
    public List<String> getText(File file);
}
