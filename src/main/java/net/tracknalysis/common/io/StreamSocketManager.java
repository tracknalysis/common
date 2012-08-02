/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author David Valeri
 */
public class StreamSocketManager implements SocketManager {
    
    private final InputStream is;
    private final OutputStream os;
    
    public StreamSocketManager(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void connect() throws IOException {
        // No-op
    }

    @Override
    public void disconnect() throws IOException {
        // No-op

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return is;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return os;
    }
}
