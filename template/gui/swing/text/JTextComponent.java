package javax.swing.text;

public abstract class JTextComponent extends JComponent {
  public JTextComponent();

  public int getCaretPosition();
  public void setCaretPosition(int position);

  public Document getDocument();
  public void setDocument(Document doc);

  public String getText();
  public void setText(String t);

  public boolean isEditable();
  public void setEditable(boolean b);

  public Insets getMargin();
  public void setMargin(Insets m);

  public Highlighter getHighlighter();
  public void setHighlighter(Highlighter h);

  public void selectAll();
}
