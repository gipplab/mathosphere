package com.formulasearchengine.mathosphere.mlp.text;
/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.parser.nodes.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A visitor to convert an article AST into a pure text representation. To
 * better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 * <ul>
 * <li><a
 * href="http://en.wikipedia.org/wiki/Visitor_pattern">http://en.wikipedia
 * .org/wiki/Visitor_pattern</a> (classic pattern)</li>
 * <li><a
 * href="http://www.javaworld.com/javaworld/javatips/jw-javatip98.html">http
 * ://www.javaworld.com/javaworld/javatips/jw-javatip98.html</a> (the version we
 * use here)</li>
 * </ul>
 *
 * The methods needed to descend into an AST and visit the children of a given
 * node <code>n</code> are
 * <ul>
 * <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node
 * <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code>
 * and gather the return values of the <code>visit()</code> calls in a list,</li>
 * <li><code>mapInPlace(n)</code> - visit the <b>children</b> of node
 * <code>n</code> and replace each child node <code>c</code> with the return
 * value of the call to <code>visit(c)</code>.</li>
 * </ul>
 */
public class MathConverter
		extends
		AstVisitor<WtNode>
{
	private List<MathTag> mathTags = new ArrayList<>();

	private static final Pattern ws = Pattern.compile("\\s+");

	private final WikiConfig config;

	private final int wrapCol;

	private boolean mathMode;

	private StringBuilder sb;

	private StringBuilder line;

	private int extLinkNum;

	/**
	 * Becomes true if we are no long at the Beginning Of the whole Document.
	 */
	private boolean pastBod;

	private int needNewlines;

	private boolean needSpace;

	private boolean noWrap;

	private LinkedList<Integer> sections;

	// =========================================================================

	public MathConverter(WikiConfig config, int wrapCol)
	{
		this.config = config;
		this.wrapCol = wrapCol;
	}

	@Override
	protected boolean before(WtNode node)
	{
		// This method is called by go() before visitation starts
		sb = new StringBuilder();
		line = new StringBuilder();
		extLinkNum = 1;
		pastBod = false;
		needNewlines = 0;
		needSpace = false;
		noWrap = false;
		mathMode = false;
		sections = new LinkedList<>();
		return super.before(node);
	}

	@Override
	protected Object after(WtNode node, Object result)
	{
		finishLine();

		// This method is called by go() after visitation has finished
		// The return value will be passed to go() which passes it to the caller
		return sb.toString();
	}

	// =========================================================================

	public void visit(WtNode n)
	{
		// Fallback for all nodes that are not explicitly handled below
//		System.out.println(n.getNodeName());
//		write("<");
//		write(n.getNodeName());
//		write(" />");
	}

	public void visit(WtNodeList n)
	{
		iterate(n);
	}

	public void visit(WtUnorderedList e)
	{
		iterate(e);
	}

	public void visit(WtOrderedList e)
	{
		iterate(e);
	}

	public void visit(WtListItem item)
	{
		newline(1);
		iterate(item);
	}

	public void visit(EngPage p)
	{
		iterate(p);
	}

	public void visit(WtText text)
	{
		write(text.getContent());
	}

	public void visit(WtWhitespace w)
	{
		write(" ");
	}

	public void visit(WtBold b)
	{
			write("\"");
			iterate(b);
			write("\"");
	}

	public void visit(WtItalics i)
	{
			write("\"");
			iterate(i);
			write("\"");
	}

	public void visit(WtXmlCharRef cr)
	{
		write(Character.toChars(cr.getCodePoint()));
	}

	public void visit(WtXmlEntityRef er)
	{
		String ch = er.getResolved();
		if (ch == null)
		{
			write('&');
			write(er.getName());
			write(';');
		}
		else
		{
			write(ch);
		}
	}

	public void visit(WtUrl wtUrl)
	{
		if (!wtUrl.getProtocol().isEmpty())
		{
			write(wtUrl.getProtocol());
			write(':');
		}
		write(wtUrl.getPath());
	}

	public void visit(WtExternalLink link)
	{
		write('[');
		write(extLinkNum++);
		write(']');
	}

	public void visit(WtInternalLink link)
	{
		write("[[");
		write(link.getTarget().getAsString());
		write("]]");
	}

	public void visit(WtSection s)
	{
		finishLine();
		StringBuilder saveSb = sb;
		boolean saveNoWrap = noWrap;

		sb = new StringBuilder();
		noWrap = true;

		iterate(s.getHeading());
		finishLine();
		String title = sb.toString().trim();

		sb = saveSb;

		if (s.getLevel() >= 1)
		{
			while (sections.size() > s.getLevel())
				sections.removeLast();
			while (sections.size() < s.getLevel())
				sections.add(1);

			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < sections.size(); ++i)
			{
				if (i < 1)
					continue;

				sb2.append(sections.get(i));
				sb2.append('.');
			}

			if (sb2.length() > 0)
				sb2.append(' ');
			sb2.append(title);
			title = sb2.toString();
		}

		newline(2);
		write(title);
		newline(1);
		write(StringUtils.strrep('-', title.length()));
		newline(2);

		noWrap = saveNoWrap;

		iterate(s.getBody());

		while (sections.size() > s.getLevel())
			sections.removeLast();
		sections.add(sections.removeLast() + 1);
	}

	public void visit(WtParagraph p)
	{
		iterate(p);
		newline(2);
	}

	public void visit(WtHorizontalRule hr)
	{
		newline(1);
		write(StringUtils.strrep('-', wrapCol));
		newline(2);
	}

	public void visit(WtXmlElement e)
	{
		if (e.getName().equalsIgnoreCase("br"))
		{
			newline(1);
		}
		else
		{
			iterate(e.getBody());
		}
	}

	// =========================================================================
	// Stuff we want to hide

	public void visit(WtImageLink n)
	{
	}

	public void visit(WtIllegalCodePoint n)
	{
	}

	public void visit(WtXmlComment n)
	{
	}
	public void visit(WtNewline n)
	{
		newline(1);
	}

	public void visit(WtTemplate n)
	{
		switch (n.getName().getAsString().toLowerCase()){
			case "math" :
				try{
					WtTemplateArgument arg0 = (WtTemplateArgument) n.getArgs().get(0);
					String content = ((WtText) arg0.getValue().get(0)).getContent().trim();
					//content = content.replaceAll("'''([a-zA-Z]+)'''","\\mathbf{$1}");
					content= content.replaceAll("[{<]sub[}>](.+?)[{<]/sub[}>]", "_{$1}")
							.replaceAll("[{<]sup[}>](.+?)[{<]/sup[}>]", "^{$1}")
							.replaceAll("'''(.+?)'''","\\\\mathbf{$1}")
							.replaceAll("''(.+?)''","$1");
					MathTag tag = new MathTag(n.getLocation().line, content, WikiTextUtils.MathMarkUpType.MATH_TEMPLATE);
					mathTags.add(tag);
					write(tag.placeholder());
				} catch (Exception e ){

				}
				break;
			case "mvar" :
				try{
					WtTemplateArgument arg0 = (WtTemplateArgument) n.getArgs().get(0);
					String content = ((WtText) arg0.getValue().get(0)).getContent().trim();
					MathTag tag = new MathTag(n.getLocation().line, content, WikiTextUtils.MathMarkUpType.MVAR_TEMPLATE);
					mathTags.add(tag);
					write(tag.placeholder());
				} catch (Exception e ){

				}
		}
	}

	public void visit(WtTemplateArgument n)
	{
		iterate(n.getValue());
	}

	public void visit(WtTemplateParameter n)
	{
	}

	public void visit(WtTagExtension n)
	{
		if (n.getName().equals("math") ){
			MathTag tag = new MathTag(n.getLocation().line, n.getBody().getContent(), WikiTextUtils.MathMarkUpType.LATEX);
			mathTags.add(tag);
			write(tag.placeholder());
		}
	}

	public void visit(WtPageSwitch n)
	{
	}

	// =========================================================================

	private void newline(int num)
	{
		if (pastBod)
		{
			if (num > needNewlines)
				needNewlines = num;
		}
	}

	private void wantSpace()
	{
		if (pastBod)
			needSpace = true;
	}

	private void finishLine()
	{
		sb.append(line.toString());
		line.setLength(0);
	}

	private void writeNewlines(int num)
	{
		finishLine();
		sb.append(StringUtils.strrep('\n', num));
		needNewlines = 0;
		needSpace = false;
	}

	private void writeWord(String s)
	{
		int length = s.length();
		if (length == 0)
			return;

		if (!noWrap && needNewlines <= 0)
		{
			if (needSpace)
				length += 1;

			if (line.length() + length >= wrapCol && line.length() > 0)
				writeNewlines(1);
		}

		if (needSpace && needNewlines <= 0)
			line.append(' ');

		if (needNewlines > 0)
			writeNewlines(needNewlines);

		needSpace = false;
		pastBod = true;
		line.append(s);
	}

	private void write(String s)
	{
		if (s.isEmpty())
			return;

		if (Character.isSpaceChar(s.charAt(0)))
			wantSpace();

		String[] words = ws.split(s);
		for (int i = 0; i < words.length;)
		{
			writeWord(words[i]);
			if (++i < words.length)
				wantSpace();
		}

		if (Character.isSpaceChar(s.charAt(s.length() - 1)))
			wantSpace();
	}

	private void write(char[] cs)
	{
		write(String.valueOf(cs));
	}

	private void write(char ch)
	{
		writeWord(String.valueOf(ch));
	}

	private void write(int num)
	{
		writeWord(String.valueOf(num));
	}
}


