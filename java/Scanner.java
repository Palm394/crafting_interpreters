import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
          start = current;
          scanToken();
        }
    
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
      char c = nextToken();
      switch (c) {
        case '(': addToken(TokenType.LEFT_PAREN); break;
        case ')': addToken(TokenType.RIGHT_PAREN); break;
        case '{': addToken(TokenType.LEFT_BRACE); break;
        case '}': addToken(TokenType.RIGHT_BRACE); break;
        case ',': addToken(TokenType.COMMA); break;
        case '.': addToken(TokenType.DOT); break;      
        case '-': addToken(TokenType.MINUS); break;
        case '+': addToken(TokenType.PLUS); break;
        case ';': addToken(TokenType.SEMICOLON); break;
        case '*': addToken(TokenType.STAR); break;
        case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
        case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
        case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
        case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
        case '/':
          if (match('/')) {
            while(peek() != '\n' && !isAtEnd()) {
              nextToken();
            }
          } else {
            addToken(TokenType.SLASH);
          }
          break;
        case '"': createStringToken(); break;
        case ' ': break;
        case '\t': break;
        case '\n': line++; break;
        default:
          if (isDigit(c)) {
            createNumberToken();
            return;
          }
          if (isAlphabet(c)) {
            identifier();
            return;
          }
          Lox.error(line, String.format("Unexpected '%c' character.", c));
      }
    }

    private char nextToken() {
      return source.charAt(current++);
    }

    private void addToken(TokenType type) {
      addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
      String text = source.substring(start, current);
      tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
      return source.length() <= current;
    }

    private boolean match(char expected) {
      if (isAtEnd()) return false;
      // current has already increment ahead in scanToken() with nextToken()
      // so it doesn't need to do here
      if (source.charAt(current) != expected) return false;
  
      current++;
      return true;
    }

    private void createStringToken() {
      while (peek() != '"' && !isAtEnd()) {
        if (peek() == '\n') line++;
        nextToken();
      }

      if (isAtEnd()) {
        Lox.error(line, "Unterminated string.");
      }

      // completely closing string part
      nextToken();

      String value = source.substring(start + 1, current - 1);
      addToken(TokenType.STRING, value);
    }

    private void createNumberToken() {
      while (isDigit(peek())) nextToken();

      // fractional part
      if (peek() == '.' && isDigit(peekNext())) {
        nextToken(); // consume '.'
        while (isDigit(peek())) nextToken();
      }

      addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));;
    }

    private void identifier() {
      while(isAlphabet_OR_Digit(peek())) nextToken();

      String text = source.substring(start, current);
      TokenType type = keywords.get(text);
      if (type == null) type = TokenType.IDENTIFIER;
      addToken(type);
    }

    private boolean isAlphabet_OR_Digit(char c) {
      return isAlphabet(c) || isDigit(c);
    }

    private boolean isAlphabet(char c) {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
      return c >= '0' && c <= '9';
    }
    
    // look like match but unconsumed!
    private char peek() {
      if (isAtEnd()) return '\0';
      return source.charAt(current);
    }

    private char peekNext() {
      if (current + 1 >= source.length()) return '\0';
      return source.charAt(current + 1);
    }

    static {
      keywords = new HashMap<>();
      keywords.put("and",   TokenType.AND);
      keywords.put("class", TokenType.CLASS);
      keywords.put("else",  TokenType.ELSE);
      keywords.put("false", TokenType.FALSE);
      keywords.put("for",   TokenType.FOR);
      keywords.put("fun",   TokenType.FUN);
      keywords.put("if",    TokenType.IF);
      keywords.put("nil",   TokenType.NIL);
      keywords.put("or",    TokenType.OR);
      keywords.put("print", TokenType.PRINT);
      keywords.put("return",TokenType.RETURN);
      keywords.put("super", TokenType.SUPER);
      keywords.put("this",  TokenType.THIS);
      keywords.put("true",  TokenType.TRUE);
      keywords.put("var",   TokenType.VAR);
      keywords.put("while", TokenType.WHILE);
  }
}
