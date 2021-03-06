package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic
import net.miginfocom.swt.MigLayout

import org.eclipse.jface.viewers.ComboViewer
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.events.ControlAdapter
import org.eclipse.swt.events.ControlEvent
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Scale
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.TableColumn
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.ToolBar
import org.eclipse.swt.widgets.ToolItem

import com.novocode.naf.swt.custom.LiveSashForm

class MainView extends Composite {
    TabItem refti
    TabItem logti
    TabFolder footerTabs
    final String style2 = "<style>body { font-family: 'Lucida Grande', Helvetica, Arial; font-size: 10pt }</style>"
    Browser reference
    Label removeTest
    Label addTest
    Scale scale
    ToolItem playAll
    Label greenBar
    Label failures
    Label runs
    ToolItem pauseResume
    ToolItem playCurrent
    ToolItem htmlunit
    ToolItem firefox
    ToolItem chrome
    ToolItem ie8
    ToolItem ie9
    ToolItem safari
    ToolItem opera
    ToolItem iphone
    ToolItem android
    
    Button findTarget
    Browser logControl
    Combo baseURL
    Text source
    Label removeTestCase
    Label addTestCase
    Label duplicateTestCase
    Label clearTestCase
    Label refreshTestCases
    Combo command
    Combo target
    Text value
    TableViewer testCasesViewer
    TableViewer testCaseViewer
    
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    Log log = new Log()
    
    class Log {
        LogLevel logLevel = LogLevel.INFO
        MainView view
        
        def debug(String text) {
            if(logLevel == LogLevel.DEBUG) {
                doLog("[debug] $text", "black")
            }
        }
        
        def info(String text) {
            if(logLevel <= LogLevel.INFO) {
                doLog("[info] $text", "black")
            }
        }
        
        def warn(String text) {
            if(logLevel <= LogLevel.WARN) {
                doLog("[warn] $text", "yellow")
            }
        }
        
        def error(String text) {
            if(logLevel <= LogLevel.ERROR) {
                doLog("[error] $text", "red")
            }
        }
        
        def doLog(String text,String color) {
            view.footerTabs.selection = [view.logti] as TabItem[]
            text = text.replaceAll('\"','\\\\"')
            text = text.replaceAll("\n",'\\\\n')
            try {
                view.logControl.evaluate($/
                    document.body.innerHTML += "<div style='color:${color}; border-bottom:1px solid #ccc'>$text</div>";
                    window.scrollTo(0, document.body.scrollHeight);
                /$)
            } catch(e) {
                println "\n\n************* FAILED JS\n\n[${text}]"
            }
        }
        
        def clear() {
            view.logControl.text = ""
        }
    }
    
    public MainView(Composite parent, int style) {
        super(parent, style)
        log.view = this
    }
    
    static class TableSizer extends ControlAdapter {
        TableColumn column
        Composite testCasesHolder
        
        TableSizer(Composite testCasesHolder, TableColumn column) {
            super();
            this.column = column;
            this.testCasesHolder = testCasesHolder;
        }

        void controlResized( ControlEvent e ) { 
            column.width = testCasesHolder.getBounds().width - 30
        }
    }
    
    void createContents() {
        setLayout(new MigLayout("inset 2", "[][grow,fill][grow,fill]", "[][][fill,grow]"))
        
        def label = new Label(this, SWT.NONE)
        label.text = "Base URL"
        
        baseURL = new Combo(this, SWT.NONE)
        baseURL.layoutData = "span 2, wrap"
        
        Display.getDefault().asyncExec {
            baseURL.setFocus()
        }
        
        
        ToolBar toolBar = new ToolBar (this, SWT.FLAT)
        toolBar.layoutData = "growx, span 2, wrap"
        
        ToolItem item = new ToolItem (toolBar, SWT.SEPARATOR)
        scale = new Scale(toolBar, SWT.BORDER)
        scale.setSize(100, 16)
        scale.setMaximum (100)
        scale.setPageIncrement(5)
        item.setWidth(100)
        item.setControl(scale)
        
        
        playAll = new ToolItem (toolBar, SWT.PUSH)
        playAll.image = Resources.getImage("gfx/PlayAll.png")
        playAll.text = "Play All"
        
        playCurrent = new ToolItem (toolBar, SWT.PUSH)
        playCurrent.image = Resources.getImage("gfx/PlayOne.png")
        playCurrent.text = "Play One"
        
        pauseResume = new ToolItem (toolBar, SWT.PUSH)
        pauseResume.image = Resources.getImage("gfx/Pause.png")
        pauseResume.text = "Pause"
        
        item = new ToolItem (toolBar, SWT.PUSH)
        item.image = Resources.getImage("gfx/Step.png")
        item.enabled = false
        item.text = "Step"
        
        item = new ToolItem (toolBar, SWT.SEPARATOR)
        
        htmlunit = new ToolItem (toolBar, SWT.RADIO)
        htmlunit.image = Resources.getImage("gfx/htmlunit.png")
        htmlunit.selection = true
        htmlunit.text = "htmlunit"
        
        firefox = new ToolItem (toolBar, SWT.RADIO)
        firefox.image = Resources.getImage("gfx/firefox.png")
        firefox.text = "firefox"
        
        chrome = new ToolItem (toolBar, SWT.RADIO)
        chrome.image = Resources.getImage("gfx/chrome.png")
        chrome.text = "chrome"
        
        safari = new ToolItem (toolBar, SWT.RADIO)
        safari.image = Resources.getImage("gfx/safari.png")
        safari.text = "safari"
        
        opera = new ToolItem (toolBar, SWT.RADIO)
        opera.image = Resources.getImage("gfx/opera.png")
        opera.text = "opera"
        
        ie8 = new ToolItem (toolBar, SWT.RADIO)
        ie8.image = Resources.getImage("gfx/ie8.png")
        ie8.text = "IE 8"
        //ie8.enabled = false
        
        ie9 = new ToolItem (toolBar, SWT.RADIO)
        ie9.image = Resources.getImage("gfx/ie9.png")
        ie9.text = "IE 9"
        ie9.enabled = false
        
        
        
        iphone = new ToolItem (toolBar, SWT.RADIO)
        iphone.image = Resources.getImage("gfx/iphone.png")
        iphone.text = "iOS"
        
        android = new ToolItem (toolBar, SWT.RADIO)
        android.image = Resources.getImage("gfx/android.png")
        android.text = "android"
        
        SashForm vform = new SashForm(this, SWT.VERTICAL)
        vform.layoutData = "span 3, w 100%, wmin 0, hmax 100%-50" // tweak to allow correct layout
        
        LiveSashForm form = new LiveSashForm(vform, SWT.HORIZONTAL)
        //form.setBackground(Display.default.getSystemColor(SWT.COLOR_RED))
        
        //
        // Test Cases
        //
        final Composite testCasesHolder = new Composite(form, SWT.NONE)
        //testCasesHolder.setBackground(Display.default.getSystemColor(SWT.COLOR_BLUE))
        testCasesHolder.layout = new MigLayout("inset 0 4 4 10, gap 0", "[grow][]", "[fill, grow][fill][][][]")
        
        testCasesViewer = new TableViewer(testCasesHolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER)
        final Table testCases = testCasesViewer.table
        testCases.layoutData = "span 2, growx, wrap, hmax 100%-100"
        testCases.setLinesVisible (false)
        testCases.setHeaderVisible (true)
        
        final TableColumn column = new TableColumn (testCases, SWT.NONE)
        column.resizable = false
        column.text = "Test Case"
        column.width = 200
        
        testCasesHolder.addControlListener(new TableSizer(testCasesHolder, column))
        
        Composite testCaseTools = new Composite(testCasesHolder, SWT.FLAT)
        testCaseTools.layoutData = "span 2, wrap, gap 0, growx"
        testCaseTools.layout = new MigLayout("inset 0, gap 0")
        
        ToolLabels tl = new ToolLabels(testCaseTools)
        addTestCase = tl.label(tip: "Add Test Case", image:"gfx/button_add.png")
        removeTestCase = tl.label(tip: "Remove Test Case", image:"gfx/button_remove.png")
        /*
        duplicateTestCase = tl.label(tip: "Remove Test Case", image:"gfx/button_duplicate.png")
        clearTestCase = tl.label(tip: "Clear all Test Cases", image:"gfx/button_clear.png")
        refreshTestCases = tl.label(tip: "Refresh Test Cases", image:"gfx/button_refresh.png")
        */
        
        greenBar = new Label(testCasesHolder, SWT.NONE)
        greenBar.layoutData = "span 2, growx, wrap, gap 0 0 5 5"
        greenBar.setBackgroundImage(Resources.getImage("gfx/progress-background.png"))
        
        new Label(testCasesHolder, SWT.NONE).text = "Runs:"
        runs = new Label(testCasesHolder, SWT.NONE)
        runs.layoutData = "wrap"
        runs.setForeground(Resources.getColor(0x4A, 0x88, 0x00))
        runs.text = "0"
        
        new Label(testCasesHolder, SWT.NONE).text = "Failures:"
        failures = new Label(testCasesHolder, SWT.NONE)
        failures.layoutData = "wrap,gap 0 0 5 5"
        failures.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED))
        failures.text = "0"
        
        //
        // Test Case
        //
        TabFolder tabFolder = new TabFolder (form, SWT.BORDER)
        
        // Table
        TabItem tbItem = new TabItem (tabFolder, SWT.NONE)
        tbItem.text = "Table"
        Composite tableHolder = new Composite(tabFolder, SWT.NONE)
        tableHolder.layout = new MigLayout("inset 2","[][grow][]", "[fill, grow][][][][]")
        
        testCaseViewer = new TableViewer(tableHolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI)
        Table table = testCaseViewer.table 
        table.layoutData = "span 3, growx, wrap, hmax 100%-100"
        table.linesVisible = true
        table.headerVisible = true
        ["Command": 120, "Target": 200, "Value": 100].each { String text, int width ->
            TableColumn col = new TableColumn (table, SWT.NONE)
            col.text = text
            col.width = width
        }
        Composite testTools = new Composite(tableHolder, SWT.NONE) 
        ToolLabels testToolLabels = new ToolLabels(testTools)
        addTest = testToolLabels.label(tip: "Add Test", image:"gfx/button_add.png")
        removeTest = testToolLabels.label(tip: "Remove Test", image:"gfx/button_remove.png")
        testTools.layoutData = "span 3, wrap, growx"
        testTools.layout = new MigLayout("inset 0, gap 0")
        
        // Command
        new Label(tableHolder, SWT.NONE).text = "Command"
        command = new Combo(tableHolder, SWT.NONE)
        command.layoutData = "span 2, wrap, growx"
        
        // Target
        new Label(tableHolder, SWT.NONE).text = "Target"
        target = new Combo(tableHolder, SWT.NONE)
        target.layoutData = "growx"
        findTarget = new Button(tableHolder, SWT.PUSH)
        findTarget.layoutData = "wrap"
        findTarget.text = "    Find    "
        
        // Value
        new Label(tableHolder, SWT.NONE).text = "Value"
        value = new Text(tableHolder, SWT.BORDER )
        value.layoutData = "span 2, growx"
        
        tbItem.control = tableHolder
        
        // Source
        TabItem tbItem2 = new TabItem (tabFolder, SWT.NONE)
        tbItem2.text = "Source"
        source = new Text(tabFolder, SWT.BORDER |  SWT.H_SCROLL | SWT.V_SCROLL )
       
        tbItem2.control = source
        
        tabFolder.pack()
        
        
        form.weights = [30, 70]
        
        footerTabs = new TabFolder(vform, SWT.BORDER)
        logti = new TabItem(footerTabs, SWT.NONE)
        logti.text = "Log"
        logControl = new Browser(footerTabs, SWT.BORDER)
        logControl.text = style
        logti.control = logControl
        
        refti = new TabItem(footerTabs, SWT.NONE)
        refti.text = "Reference"
        reference = new Browser(footerTabs, SWT.BORDER)
        reference.text = ""
        refti.control = reference
        
//        TabItem uielti = new TabItem(footerTabs, SWT.NONE)
//        uielti.text = "UI-Element"
//        Browser uiElement = new Browser(footerTabs, SWT.BORDER)
//        uiElement.text = style+"<h2>UI-Element todo...</h2>"
//        uielti.control = uiElement
        
        footerTabs.pack()
        
        vform.weights = [80, 20]
        
        layout()
    }
    
    void setReferenceHTML(String html) {
        footerTabs.selection = [refti] as TabItem[]
        reference.text = style+html
    }
}