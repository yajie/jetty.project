//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.start;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple Start .INI handler
 */
public class StartIni implements Iterable<String>
{
    public static interface IncludeListener
    {
        public List<StartIni> onIniInclude(String path) throws IOException;
    }

    private final File file;
    private final List<String> lines= new ArrayList<>();

    public StartIni(File file) throws FileNotFoundException, IOException
    {
        this.file = file;
        try (FileReader reader = new FileReader(file))
        {
            try (BufferedReader buf = new BufferedReader(reader))
            {
                String line;
                while ((line = buf.readLine()) != null)
                    process(line.trim());
            }
        }
    }

    public StartIni(ArrayList<String> arguments)
    {
        file=null;
        for (String line: arguments)
            process(line);
    }
    
    private void process(String line)
    {
        if (line.length() == 0)
            return;

        if (line.charAt(0) == '#')
            return;

        // Smart Handling, split into multiple OPTIONS lines (for dup check reasons)
        if (line.startsWith("OPTIONS="))
        {
            int idx = line.indexOf('=');
            String value = line.substring(idx + 1);
            for (String part : value.split(","))
            {
                addUniqueLine("OPTION=" + part);
            }
        }
        else
        {
            // Add line as-is
            addUniqueLine(line);
        }
    }

    private void addUniqueLine(String line)
    {
        if (lines.contains(line))
        {
            // skip
            return;
        }
        lines.add(line);
    }

    public File getFile()
    {
        return file;
    }

    public List<String> getLineMatches(Pattern pattern)
    {
        List<String> ret = new ArrayList<>();
        for (String line : lines)
        {
            if (pattern.matcher(line).matches())
            {
                ret.add(line);
            }
        }
        return ret;
    }

    public List<String> getLines()
    {
        return lines;
    }

    @Override
    public Iterator<String> iterator()
    {
        return lines.iterator();
    }
}