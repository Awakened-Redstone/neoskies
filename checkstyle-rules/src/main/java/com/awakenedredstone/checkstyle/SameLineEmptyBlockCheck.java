package com.awakenedredstone.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CodePointUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

import java.util.Arrays;
import java.util.Optional;

public class SameLineEmptyBlockCheck extends AbstractCheck {
    @Override
    public int[] getDefaultTokens() {
        return new int[] {
          TokenTypes.LITERAL_WHILE,
          TokenTypes.LITERAL_TRY,      // try blocks
          TokenTypes.LITERAL_CATCH,    // catch blocks
          TokenTypes.LITERAL_FINALLY,  // finally blocks
          TokenTypes.LITERAL_DO,
          TokenTypes.LITERAL_IF,
          TokenTypes.LITERAL_ELSE,
          TokenTypes.LITERAL_FOR,
          TokenTypes.INSTANCE_INIT,
          TokenTypes.STATIC_INIT,
          TokenTypes.LITERAL_SWITCH,
          TokenTypes.LITERAL_SYNCHRONIZED,
          TokenTypes.LITERAL_CASE,
          TokenTypes.LITERAL_DEFAULT,
          TokenTypes.ARRAY_INIT,

          TokenTypes.CTOR_DEF,         // Constructors
          TokenTypes.METHOD_DEF,       // Methods
          TokenTypes.CLASS_DEF,        // Classes
          TokenTypes.INTERFACE_DEF,    // Interfaces
          TokenTypes.ENUM_DEF,         // Enums
          TokenTypes.RECORD_DEF,       // Records
          TokenTypes.SLIST,            // Standalone blocks
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        final Optional<DetailAST> leftCurly = getLeftCurly(ast);
        if (leftCurly.isPresent()) {
            final DetailAST leftCurlyAST = leftCurly.orElseThrow();

            final boolean emptyBlock;
            if (leftCurlyAST.getType() == TokenTypes.LCURLY) {
                final DetailAST nextSibling = leftCurlyAST.getNextSibling();
                emptyBlock = nextSibling.getType() != TokenTypes.CASE_GROUP && nextSibling.getType() != TokenTypes.SWITCH_RULE;
            } else {
                emptyBlock = leftCurlyAST.getChildCount() <= 1;
            }

            if (emptyBlock) {
                final DetailAST rightCurlyAST = getRightCurly(leftCurlyAST);
                if (rightCurlyAST == null) {
                    return;
                }

                if (hasText(leftCurlyAST)) {
                    return;
                }

                if (leftCurlyAST.getLineNo() != rightCurlyAST.getLineNo()) {
                    log(leftCurlyAST, "Empty block should be on a single line.");
                } else {
                    final int[] codeBetweenBraces = Arrays.copyOfRange(getLineCodePoints(leftCurlyAST.getLineNo() - 1), leftCurlyAST.getColumnNo() + 1, rightCurlyAST.getColumnNo());
                    if (codeBetweenBraces.length < 1 || (codeBetweenBraces.length > 1 && codeBetweenBraces[0] != 32)) { // Check if only one whitespace exists
                        log(leftCurlyAST, "Empty block should be on a single line with no spaces between braces.");
                    }
                }
            }
        }
    }

    /**
     * Checks if SLIST token contains any text.
     *
     * @param slistAST a {@code DetailAST} value
     * @return whether the SLIST token contains any text.
     */
    private boolean hasText(final DetailAST slistAST) {
        final DetailAST rightCurly = slistAST.findFirstToken(TokenTypes.RCURLY);
        final DetailAST rightCurlyAST;

        if (rightCurly == null) {
            rightCurlyAST = slistAST.getParent().findFirstToken(TokenTypes.RCURLY);
        } else {
            rightCurlyAST = rightCurly;
        }
        final int slistLineNo = slistAST.getLineNo();
        final int slistColNo = slistAST.getColumnNo();
        final int rightCurlyLineNo = rightCurlyAST.getLineNo();
        final int rightCurlyColNo = rightCurlyAST.getColumnNo();
        boolean returnValue = false;
        if (slistLineNo == rightCurlyLineNo) {
            // Handle braces on the same line
            final int[] txt = Arrays.copyOfRange(getLineCodePoints(slistLineNo - 1), slistColNo + 1, rightCurlyColNo);

            if (!CodePointUtil.isBlank(txt)) {
                returnValue = true;
            }
        } else {
            final int[] codePointsFirstLine = getLineCodePoints(slistLineNo - 1);
            final int[] firstLine = Arrays.copyOfRange(codePointsFirstLine, slistColNo + 1, codePointsFirstLine.length);
            final int[] codePointsLastLine = getLineCodePoints(rightCurlyLineNo - 1);
            final int[] lastLine = Arrays.copyOfRange(codePointsLastLine, 0, rightCurlyColNo);
            // check if all lines are also only whitespace
            returnValue = !(CodePointUtil.isBlank(firstLine) && CodePointUtil.isBlank(lastLine)) || !checkIsAllLinesAreWhitespace(slistLineNo, rightCurlyLineNo);
        }
        return returnValue;
    }

    /**
     * Checks is all lines from 'lineFrom' to 'lineTo' (exclusive)
     * contain whitespaces only.
     *
     * @param lineFrom
     *            check from this line number
     * @param lineTo
     *            check to this line numbers
     * @return true if lines contain only whitespaces
     */
    private boolean checkIsAllLinesAreWhitespace(int lineFrom, int lineTo) {
        boolean result = true;
        for (int i = lineFrom; i < lineTo - 1; i++) {
            if (!CodePointUtil.isBlank(getLineCodePoints(i))) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static Optional<DetailAST> getLeftCurly(DetailAST ast) {
        final DetailAST parent = ast.getParent();
        final int parentType = parent.getType();
        final Optional<DetailAST> leftCurly;

        if (parentType == TokenTypes.SWITCH_RULE) {
            // get left curly of a case or default that is in switch rule
            leftCurly = Optional.ofNullable(parent.findFirstToken(TokenTypes.SLIST));
        } else if (parentType == TokenTypes.CASE_GROUP) {
            // get left curly of a case or default that is in switch statement
            leftCurly = Optional.ofNullable(ast.getNextSibling())
              .map(DetailAST::getFirstChild)
              .filter(node -> node.getType() == TokenTypes.SLIST);
        } else if (ast.findFirstToken(TokenTypes.SLIST) != null) {
            // we have a left curly that is part of a statement list, but not in a case or default
            leftCurly = Optional.of(ast.findFirstToken(TokenTypes.SLIST));
        } else {
            // get the first left curly that we can find, if it is present
            leftCurly = Optional.ofNullable(ast.findFirstToken(TokenTypes.LCURLY));
        }
        return leftCurly;
    }

    private static DetailAST getRightCurly(DetailAST leftCurly) {
        DetailAST rightCurly = leftCurly.findFirstToken(TokenTypes.RCURLY);
        if (rightCurly == null) {
            return leftCurly.getParent().findFirstToken(TokenTypes.RCURLY);
        } else {
            return rightCurly;
        }
    }
}
