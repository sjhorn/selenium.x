package com.hornmicro.selenium.ui

import net.miginfocom.swt.MigLayout

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.events.ControlAdapter
import org.eclipse.swt.events.ControlEvent
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.events.PaintListener
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Canvas
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
    
    public MainView(Composite parent, int style) {
        super(parent, style)

    }
    
    
    
    void createContents() {
        setLayout(new MigLayout("inset 2", "[][grow,fill][grow,fill]", "[][][fill,grow]"))
        
        def label = new Label(this, SWT.NONE)
        label.text = "Base URL"
        label.setFocus()
        
        def combo = new Combo(this, SWT.NONE)
        combo.text = "Hello there mate 2!"
        combo.items = ["Hello world","Yet another"]
        combo.layoutData = "span 2, wrap"
        
        
        ToolBar toolBar = new ToolBar (this, SWT.NONE)
        toolBar.layoutData = "growx, span 2, wrap"
        
        ToolItem item = new ToolItem (toolBar, SWT.SEPARATOR)
        Scale scale = new Scale(toolBar, SWT.BORDER)
        scale.setSize(100, 16)
        scale.setMaximum (100)
        scale.setPageIncrement(5)
        item.setWidth(100)
        item.setControl(scale)
        
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def playAll = new Image(Display.default, "gfx/PlayAll.png")
        item.image = playAll
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def playOne = new Image(Display.default, "gfx/PlayOne.png")
        item.image = playOne
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def pause = new Image(Display.default, "gfx/Pause.png")
        item.image = pause
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def stp = new Image(Display.default, "gfx/Step.png")
        item.image = stp
        
        item = new ToolItem (toolBar, SWT.PUSH)
        def cont = new Image(Display.default, "gfx/Continue.png")
        item.image = cont
        
        SashForm vform = new SashForm(this,SWT.VERTICAL)
        vform.layoutData = "span 3, w 100%"
        
        LiveSashForm form = new LiveSashForm(vform, SWT.HORIZONTAL)
        //form.setBackground(Display.default.getSystemColor(SWT.COLOR_RED))
        form.layoutData = ""
        
        //
        // Test Cases
        //
        Composite testCasesHolder = new Composite(form, SWT.NONE)
        //testCasesHolder.setBackground(Display.default.getSystemColor(SWT.COLOR_BLUE))
        testCasesHolder.layout = new MigLayout("inset 0 4 4 10", "[grow][]", "[fill, grow][][][]")
        
        final Table testCases = new Table(testCasesHolder, SWT.BORDER | SWT.FULL_SELECTION)
        testCases.layoutData = "span 2, growx,wrap"
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
        
        
        def bar = new Label(testCasesHolder, SWT.NONE)
        bar.layoutData = "span 2, growx, wrap"
        bar.setBackgroundImage(new Image(Display.default, "gfx/progress-background.png"))
        
        new Label(testCasesHolder, SWT.NONE).text = "Runs:"
        Label runs = new Label(testCasesHolder, SWT.NONE)
        runs.layoutData = "wrap"
        runs.setForeground(Display.default.getSystemColor(SWT.COLOR_GREEN))
        runs.text = "0"
        
        new Label(testCasesHolder, SWT.NONE).text = "Failures:"
        Label failures = new Label(testCasesHolder, SWT.NONE)
        failures.layoutData = "wrap"
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
        tableHolder.layout = new MigLayout("inset 2","[][grow][]", "[fill, grow][]")
        
        Table table = new Table (tableHolder, SWT.BORDER | SWT.FULL_SELECTION)
        table.layoutData = "span 3, growx, wrap"
        table.linesVisible = true
        table.headerVisible = true
        ["Command", "Target", "Value"].each {
            TableColumn col = new TableColumn (table, SWT.NONE)
            col.text = it
            col.width = 160
        }
        
        // Command
        new Label(tableHolder, SWT.NONE).text = "Command"
        Combo command = new Combo(tableHolder, SWT.NONE)
        command.layoutData = "span 2, wrap, growx"
        
        // Target
        new Label(tableHolder, SWT.NONE).text = "Target"
        Combo target = new Combo(tableHolder, SWT.NONE)
        target.layoutData = "growx"
        Button find = new Button(tableHolder, SWT.PUSH)
        find.layoutData = "wrap"
        find.text = "    Find    "
        
        // Value
        new Label(tableHolder, SWT.NONE).text = "Value"
        Text value = new Text(tableHolder, SWT.BORDER)
        value.layoutData = "span 2, growx"
        
        tbItem.control = tableHolder
        
        // Source
        TabItem tbItem2 = new TabItem (tabFolder, SWT.NONE)
        tbItem2.text = "Source"
        Text text = new Text(tabFolder, SWT.BORDER | SWT.MULTI)
       
        tbItem2.control = text
        
        tabFolder.pack()
        
        
        form.weights = [30, 70]
        
        TabFolder footerTabs = new TabFolder(vform, SWT.BORDER);
        TabItem logti = new TabItem(footerTabs, SWT.NONE)
        logti.text = "Log"
        
        TabItem refti = new TabItem(footerTabs, SWT.NONE)
        refti.text = "Reference"
        
        TabItem uielti = new TabItem(footerTabs, SWT.NONE)
        uielti.text = "UI-Element"
        
        footerTabs.pack()
        
        vform.weights = [80, 20]
        
        layout()
    }
}

class LineSeparator extends Canvas implements PaintListener {

    LineSeparator(parent) {
        super(parent, SWT.NONE)
        addPaintListener(this)
    }
    
    public void paintControl(PaintEvent pe) {
        
    }
    
    
}