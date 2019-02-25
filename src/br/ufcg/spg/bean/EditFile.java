package br.ufcg.spg.bean;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class EditFile {
	private String srcPath;
	private String dstPath;
	
	private Tuple<String, String> beforeAfter;
	private List<Tuple<ASTNode, ASTNode>> edits;
	
	public EditFile() {
	}

	public EditFile(Tuple<String, String> beforeAfter
			, List<Tuple<ASTNode, ASTNode>> edits, String srcPath, String dstPath) {
		super();
		this.beforeAfter = beforeAfter;
		this.edits = edits;
		this.srcPath = srcPath;
		this.dstPath = dstPath;
	}
	
	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public String getDstPath() {
		return dstPath;
	}

	public void setDstPath(String dstPath) {
		this.dstPath = dstPath;
	}

	public Tuple<String, String> getBeforeAfter() {
		return beforeAfter;
	}
	public void setBeforeAfter(Tuple<String, String> beforeAfter) {
		this.beforeAfter = beforeAfter;
	}
	public List<Tuple<ASTNode, ASTNode>> getEdits() {
		return edits;
	}
	public void setEdits(List<Tuple<ASTNode, ASTNode>> edits) {
		this.edits = edits;
	}
}
