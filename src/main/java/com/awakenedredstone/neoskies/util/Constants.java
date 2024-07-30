package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.NeoSkies;
import com.ezylang.evalex.config.ExpressionConfiguration;

public class Constants {
    public static final String NAMESPACE = NeoSkies.MOD_ID;
    public static final String NAMESPACE_NETHER = NeoSkies.MOD_ID + "_nether";
    public static final String NAMESPACE_END = NeoSkies.MOD_ID + "_end";
    public static final ExpressionConfiguration EXPRESSION_PARSER = ExpressionConfiguration.builder()
      .arraysAllowed(false)
      .structuresAllowed(false)
      .build();
}
