package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	private static ArrayList<String> visitedURLs = new ArrayList<String>();
    private static ArrayDeque<String> parenStack = new ArrayDeque<String>();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

        String dest = "https://en.wikipedia.org/wiki/Philosophy";
		String start = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		System.out.println("Starting on: "+start);
		Integer limit = 20;

		testPhilosophyConjecture(dest, start, limit);
	}

	public static void testPhilosophyConjecture(String dest, String start, Integer limit) throws IOException {
		String url = start;
		for (int i=0; i<limit; i++) {
			if (visitedURLs.contains(url)) {
				System.out.println("In a loop!");
				return;
			}
			else {
				visitedURLs.add(url);
			}

			Elements paragraphs = wf.fetchWikipedia(url);
			Element firstLink = findFirstLink(paragraphs);

			if (firstLink == null) {
				System.out.println("Got to a page with no valid links");
				return;
			}

			System.out.println(firstLink.text());
			url = firstLink.absUrl("href");;

			if (url.equals(dest)) {
				System.out.println("We are on the 'Philosophy' Wikipedia page! Woohoo!");
				return;
			}
		}
	}

	public static Element findFirstLink(Elements paragraphs) {
		for (Element paragraph: paragraphs) {
			Iterable<Node> iter = new WikiNodeIterable(paragraph);
			for (Node node: iter) {
				if (node instanceof TextNode) {
					checkTextNode((TextNode)node);
				}

				if (node instanceof Element) {
					Element validLink = isValidLink((Element) node);
					if (validLink != null) {
						return validLink;
					}
				}
	        }
		}
		return null;
	}

	private static void checkTextNode(TextNode node) {
		// populate parenStack
		String text = node.text();
		for (int i=0; i<text.length(); i++) {
			if (text.charAt(i)=='(') {
				parenStack.push("(");
			}
			if (text.charAt(i)==')') {
				if (parenStack.isEmpty()) {
					System.out.println("Unbalanced Parenthesis: Encountered ')' before '('");
				}
				parenStack.pop();
			}
		}

	}

	private static Element isValidLink(Element node) {
		// Check if is a valid link
		if (!node.tagName().equals("a")) {
			return null;
		}
		if (Character.isUpperCase(node.text().charAt(0))) {
			return null;
		}
		if (!node.attr("href").startsWith("/wiki/")){
			return null;
		}
		if (node.attr("href").endsWith("redlink=1")) {
			return null;
		}
		if (isItalicized(node)) {
			return null;
		}
		if (isInParen(node)) {
			return null;
		}
		return node;
	}

	private static boolean isItalicized(Element first) {
		// check if is italicized
		for (Element node=first; node!=null; node=node.parent()) {
			if (node.tagName().equals("i") || node.tagName().equals("em")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInParen(Element node) {
		// check if is in parenthesis
		return !parenStack.isEmpty();
	}
}
