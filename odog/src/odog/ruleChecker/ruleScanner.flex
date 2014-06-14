package odog.ruleChecker;

import java_cup.runtime.*;

/**
 * This class is a simple example lexer.
 */
%%

%class Lexer
%unicode
%cup
%line
%column

%{
/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }

%}

LineTerminator = \r | \n| \r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

Identifier = [:jletter:] [:jletterdigit:]*

%state STRING

%%

<YYINITIAL> "dport"              { return symbol(sym.DPORT); }
<YYINITIAL> "atomicComponent"        { return symbol(sym.ACOMP); }
<YYINITIAL> "attribute"          { return symbol(sym.ATTR); }
<YYINITIAL> "topology"           { return symbol(sym.TOPOLOGY); }
<YYINITIAL> "compInstance"      { return symbol(sym.COMPONENTINSTANCE); }
<YYINITIAL> "attributeClass"     { return symbol(sym.ATTRCLASS); }
<YYINITIAL> "atomicVersion"      { return symbol(sym.VER); }
<YYINITIAL> "topologyVersion"    { return symbol(sym.HVER); }
<YYINITIAL> "value"              { return symbol(sym.VALUE); }
<YYINITIAL> "reqserv"            { return symbol(sym.REQSERV); }
<YYINITIAL> "method"             { return symbol(sym.METHOD); }
<YYINITIAL> "connection"         { return symbol(sym.CONNECTION); }
<YYINITIAL> "exportedPort"       { return symbol(sym.EXPORTEDPORT); }
<YYINITIAL> "defVer"             { return symbol(sym.DEFVER); }

<YYINITIAL> "PT"              { return symbol(sym.PT_QUAL); }
<YYINITIAL> "EX"              { return symbol(sym.EX_QUAL); }
<YYINITIAL> "->"              { return symbol(sym.EX_AR); }
<YYINITIAL> "->*"             { return symbol(sym.EX_CM); }
<YYINITIAL> "."               { return symbol(sym.DOT); }
<YYINITIAL> "("               { return symbol(sym.LPAR); }
<YYINITIAL> ")"               { return symbol(sym.RPAR); }
<YYINITIAL> "&&"              { return symbol(sym.AND); }
<YYINITIAL> "||"              { return symbol(sym.OR); }
<YYINITIAL> "~"               { return symbol(sym.NOT); }
<YYINITIAL> "=>"              { return symbol(sym.IMPLY); }
<YYINITIAL> "<=>"             { return symbol(sym.IFONLYIF); }
<YYINITIAL> "="               { return symbol(sym.EQ); }
<YYINITIAL> "!="              { return symbol(sym.DIFF); }
<YYINITIAL> ">"               { return symbol(sym.GT); }
<YYINITIAL> ">="              { return symbol(sym.GE); }
<YYINITIAL> "<"               { return symbol(sym.LT); }
<YYINITIAL> "<="              { return symbol(sym.LE); }
<YYINITIAL> "["               { return symbol(sym.LBRAC); }
<YYINITIAL> "]"               { return symbol(sym.RBRAC); }
<YYINITIAL> ";"               { return symbol(sym.SEMICOLON); }
<YYINITIAL> ","               { return symbol(sym.COLON); }

<YYINITIAL> {Identifier}      { return symbol(sym.IDENTIFIER, yytext()); }

<YYINITIAL> \"                { string.setLength(0); yybegin(STRING); }

<YYINITIAL> {WhiteSpace}      { }

<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   String s = string.toString();
                                   string = new StringBuffer();
                                   return symbol(sym.STRING_LITERAL, s); }

  [^\n\r\"\\]+                   { string.append( yytext() ); }

  \\t                            { string.append('\t'); }
  
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

/* error fallback */
.|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+">"); }
