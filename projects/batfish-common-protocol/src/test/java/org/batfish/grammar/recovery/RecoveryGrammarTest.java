package org.batfish.grammar.recovery;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.util.CommonUtil;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.recovery.RecoveryParser.RecoveryContext;
import org.junit.Test;

@ParametersAreNonnullByDefault
public final class RecoveryGrammarTest {

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder().setThrowOnLexerError(true).setThrowOnParserError(true).build();

  @Test
  public void testParsingRecovery() {
    String recoveryText = CommonUtil.readResource("org/batfish/grammar/recovery/recovery_text");
    int totalLines = recoveryText.split("\n", -1).length;
    RecoveryCombinedParser cp = new RecoveryCombinedParser(recoveryText, SETTINGS);
    RecoveryContext ctx = cp.parse();
    RecoveryExtractor extractor = new RecoveryExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);

    assertThat(extractor.getFirstErrorLine(), equalTo(2));
    assertThat(extractor.getNumBlockStatements(), equalTo(2));
    assertThat(extractor.getNumErrorNodes(), equalTo(15));
    assertThat(extractor.getNumInnerStatements(), equalTo(1));
    assertThat(extractor.getNumSimpleStatements(), equalTo(5));
    assertThat(
        extractor.getNumStatements(),
        equalTo(extractor.getNumBlockStatements() + extractor.getNumSimpleStatements()));
    assertThat(extractor.getNumTailWords(), equalTo(5));
    /*
     *  We don't know how many lines were hidden comments, so we only know there should be more
     *  lines than the ones that actually end up in the parse tree.
     */
    assertThat(
        totalLines,
        greaterThanOrEqualTo(
            extractor.getNumBlockStatements()
                + extractor.getNumInnerStatements()
                + extractor.getNumSimpleStatements()
                + extractor.getNumErrorNodes()));
  }

  @Test
  public void testParsingRecoveryWithModes() {
    String recoveryText = CommonUtil.readResource("org/batfish/grammar/recovery/recovery_badmode");
    RecoveryCombinedParser cp = new RecoveryCombinedParser(recoveryText, SETTINGS);
    RecoveryContext ctx = cp.parse();
    RecoveryExtractor extractor = new RecoveryExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);

    assertThat(extractor.getFirstErrorLine(), equalTo(4));
    assertThat(extractor.getNumErrorNodes(), equalTo(1));
  }
}
