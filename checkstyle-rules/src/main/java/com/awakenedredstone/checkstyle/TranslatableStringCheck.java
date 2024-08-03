package com.awakenedredstone.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class TranslatableStringCheck extends AbstractCheck {
    private static final String MSG_KEY = "Not a valid translation key: \"{0}\"";
    private static final String DEFAULT_PATTERN = "^[a-z0-9_]+(\\.[a-z0-9_]+)*$";
    private String pattern = DEFAULT_PATTERN;

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST ast) {
        // Check if it's a call to Texts.translatable
        if (isTextsTranslatableCall(ast)) {
            // Get the argument of the Texts.translatable call
            DetailAST firstToken = ast.getFirstChild().getNextSibling().findFirstToken(TokenTypes.EXPR);
            if (firstToken == null) {
                return;
            }

            DetailAST keyArg = firstToken.getFirstChild();

            // Check if the argument is a string literal
            if (keyArg.getType() == TokenTypes.STRING_LITERAL) {
                String key = keyArg.getText().replaceAll("^\"|\"$", ""); // Remove quotes

                // Check if the key matches the pattern
                if (!key.matches(pattern)) {
                    log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, key, pattern);
                }
            }
        }
    }

    private boolean isTextsTranslatableCall(DetailAST ast) {
        return ast.getChildCount() >= 2 // At least 2 children: identifier and arguments
          && ast.getFirstChild().getType() == TokenTypes.DOT // First child is a dot
          && ast.getFirstChild().getFirstChild().getType() == TokenTypes.IDENT // Identifier before dot
          && ("Texts".equals(ast.getFirstChild().getFirstChild().getText()) || "Text".equals(ast.getFirstChild().getFirstChild().getText())) // Identifier is "Texts" or "Text"
          && "translatable".equals(ast.getFirstChild().getLastChild().getText()); // Method name is "translatable"
    }
}
