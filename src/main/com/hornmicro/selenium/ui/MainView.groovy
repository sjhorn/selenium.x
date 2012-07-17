package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic
import net.miginfocom.swt.MigLayout

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

//@CompileStatic
class MainView extends Composite {
    private Text source
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
    
    public MainView(Composite parent, int style) {
        super(parent, style)

    }
    
    void createContents() {
        setLayout(new MigLayout("inset 2", "[][grow,fill][grow,fill]", "[][][fill,grow]"))
        
        def label = new Label(this, SWT.NONE)
        label.text = "Base URL"
        
        
        final Combo combo = new Combo(this, SWT.NONE)
        
        combo.items = ["http://www.wotif.com","http://www.google.com"]
        combo.select(0)
        combo.layoutData = "span 2, wrap"
        
        Display.default.asyncExec {
            combo.setFocus()
        }
        
        
        ToolBar toolBar = new ToolBar (this, SWT.FLAT)
        toolBar.layoutData = "growx, span 2, wrap"
        
        ToolItem item = new ToolItem (toolBar, SWT.SEPARATOR)
        Scale scale = new Scale(toolBar, SWT.BORDER)
        scale.setSize(100, 16)
        scale.setMaximum (100)
        scale.setPageIncrement(5)
        item.setWidth(100)
        item.setControl(scale)
        
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def playAll = ImageFactory.getImage("gfx/PlayAll.png")
        item.image = playAll
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def playOne = ImageFactory.getImage("gfx/PlayOne.png")
        item.image = playOne
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def pause = ImageFactory.getImage("gfx/Pause.png")
        item.image = pause
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def stp = ImageFactory.getImage("gfx/Step.png")
        item.image = stp
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def cont = ImageFactory.getImage("gfx/Continue.png")
        item.image = cont
        
        SashForm vform = new SashForm(this, SWT.VERTICAL)
        vform.layoutData = "span 3, w 100%, wmin 0, hmax 100%-50" // tweak to allow correct layout
        
        LiveSashForm form = new LiveSashForm(vform, SWT.HORIZONTAL)
        //form.setBackground(Display.default.getSystemColor(SWT.COLOR_RED))
        
        //
        // Test Cases
        //
        Composite testCasesHolder = new Composite(form, SWT.NONE)
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
        column.width = 80
        
        
        testCasesHolder.addControlListener(new ControlAdapter() {
            void controlResized(ControlEvent e) {
                column.width = testCasesHolder.bounds.width - 30
            }
        })
        
        Composite testCaseTools = new Composite(testCasesHolder, SWT.FLAT)
        testCaseTools.layoutData = "span 2, wrap, gap 0, growx"
        testCaseTools.layout = new MigLayout("inset 0, gap 0")
        
        [
            addTestCase: [tip: "Add Test Case", image:"gfx/button_add.png"],
            removeTestCase: [tip: "Remove Test Case", image:"gfx/button_remove.png"],
            duplicateTestCase: [tip: "Remove Test Case", image:"gfx/button_duplicate.png"],
            clearTestCase: [tip: "Clear all Test Cases", image:"gfx/button_clear.png"],
            refreshTestCases: [tip: "Refresh Test Cases", image:"gfx/button_refresh.png"],
        ].each { name, data ->
            this[name] = new Label(testCaseTools, SWT.NONE)
            this[name].toolTipText = data.tip
            this[name].image = ImageFactory.getImage(data.image)
            this[name].cursor = Display.default.getSystemCursor(SWT.CURSOR_HAND)
        }
        
        def bar = new Label(testCasesHolder, SWT.NONE)
        bar.layoutData = "span 2, growx, wrap, gap 0 0 5 5"
        bar.setBackgroundImage(ImageFactory.getImage("gfx/progress-background.png"))
        
        new Label(testCasesHolder, SWT.NONE).text = "Runs:"
        Label runs = new Label(testCasesHolder, SWT.NONE)
        runs.layoutData = "wrap"
        runs.setForeground(Display.default.getSystemColor(SWT.COLOR_GREEN))
        runs.text = "0"
        
        new Label(testCasesHolder, SWT.NONE).text = "Failures:"
        Label failures = new Label(testCasesHolder, SWT.NONE)
        failures.layoutData = "wrap,gap 0 0 5 5"
        failures.setForeground(Display.default.getSystemColor(SWT.COLOR_RED))
        failures.text = 0
        
        //
        // Test Case
        //
        TabFolder tabFolder = new TabFolder (form, SWT.BORDER)
        //tabFolder.setBackground(Display.default.getSystemColor(SWT.COLOR_GREEN))
        
        // Table
        TabItem tbItem = new TabItem (tabFolder, SWT.NONE)
        tbItem.text = "Table"
        Composite tableHolder = new Composite(tabFolder, SWT.NONE)
        tableHolder.layout = new MigLayout("inset 2","[][grow][]", "[fill, grow][][][]")
        
        testCaseViewer = new TableViewer(tableHolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER)
        Table table = testCaseViewer.table 
        table.layoutData = "span 3, growx, wrap, hmax 100%-100"
        table.linesVisible = true
        table.headerVisible = true
        ["Command", "Target", "Value"].each {
            TableColumn col = new TableColumn (table, SWT.NONE)
            col.text = it
            col.width = 130
        }
        
        // Command
        new Label(tableHolder, SWT.NONE).text = "Command"
        command = new Combo(tableHolder, SWT.NONE)
        command.layoutData = "span 2, wrap, growx"
        
        // Target
        new Label(tableHolder, SWT.NONE).text = "Target"
        target = new Combo(tableHolder, SWT.NONE)
        target.layoutData = "growx"
        Button find = new Button(tableHolder, SWT.PUSH)
        find.layoutData = "wrap"
        find.text = "    Find    "
        
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
        
        TabFolder footerTabs = new TabFolder(vform, SWT.BORDER)
        TabItem logti = new TabItem(footerTabs, SWT.NONE)
        logti.text = "Log"
        Browser log = new Browser(footerTabs, SWT.BORDER)
        log.text = "<h2>Log todo...</h2>"
        logti.control = log
        
        TabItem refti = new TabItem(footerTabs, SWT.NONE)
        refti.text = "Reference"
        Browser reference = new Browser(footerTabs, SWT.BORDER)
        reference.text = "<h2>Reference todo...</h2>"
        refti.control = reference
        
        TabItem uielti = new TabItem(footerTabs, SWT.NONE)
        uielti.text = "UI-Element"
        Browser uiElement = new Browser(footerTabs, SWT.BORDER)
        uiElement.setText("<h2>UI-Element todo...</h2>")
        uielti.control = uiElement
        
        footerTabs.pack()
        
        vform.weights = [80, 20]
        
        layout()
    }
}