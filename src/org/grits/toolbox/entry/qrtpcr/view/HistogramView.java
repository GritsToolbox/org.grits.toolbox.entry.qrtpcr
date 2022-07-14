package org.grits.toolbox.entry.qrtpcr.view;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.qrtpcr.IColorsConstants;
import org.grits.toolbox.entry.qrtpcr.model.ChartData;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.IErrorBar;
import org.swtchart.IErrorBar.ErrorBarType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

public class HistogramView  {

	public static final String ID = "qrtPCR-histogram"; //$NON-NLS-1$
	
	private Chart chart;
	IBarSeries series;
	IAxis xAxis;
	String partLabel;
	IBarSeries[] allSeries;
	 
	
	@PostConstruct
	public void createPartControl(Composite parent) {
		chart = new InteractiveChart(parent, SWT.NONE);
	}

	@Focus
	public void setFocus() {
		chart.setFocus();
	}
	
	public String getPartName() {
		return partLabel;
	}
	public void setPartName(String partName) {
		partLabel = partName;
	}

	public void initializeChart(List<ChartData> dataList) {
        chart.getTitle().setText("");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Genes");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Average");
        IAxisSet axisSet = chart.getAxisSet();
        xAxis = axisSet.getXAxis(0);
        xAxis.enableCategory(true);
        IAxisTick xTick = axisSet.getXAxis(0).getTick();
        xTick.setTickLabelAngle(90);

        series = (IBarSeries) chart.getSeriesSet().createSeries(
        SeriesType.BAR, getPartName());
        series.setBarColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        
        setInput(dataList);
        ChartData[][] newDataList = new ChartData[1][dataList.size()];
        int i=0;
        for (ChartData data: dataList) {
        	newDataList[0][i++] = data;
        }
        addMouseOver(newDataList);
    }
	
	public void initializeChart(String[] aliasList, ChartData[][] dataList, String yAxisTitle, boolean showErrorBar) {
		// since we are using the same view multiple times to display a chart with different input
		// we need to remove any existing series
		ISeries[] existing = chart.getSeriesSet().getSeries();
		if (existing != null) {
			for (ISeries iSeries : existing) {
				chart.getSeriesSet().deleteSeries(iSeries.getId());;
			}
		}
        chart.getTitle().setText("");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Genes");
        chart.getAxisSet().getYAxis(0).getTitle().setText(yAxisTitle);
        IAxisSet axisSet = chart.getAxisSet();
        xAxis = axisSet.getXAxis(0);
        xAxis.enableCategory(true);
        IAxisTick xTick = axisSet.getXAxis(0).getTick();
        xTick.setTickLabelAngle(90);

        allSeries = new IBarSeries[aliasList.length];
        for (int i = 0; i < aliasList.length; i++) {
        	IBarSeries series = (IBarSeries) chart.getSeriesSet().createSeries(SeriesType.BAR, aliasList[i]);
        	if (i < IColorsConstants.COLORS.length)
        		series.setBarColor(new Color(Display.getCurrent(),IColorsConstants.COLORS[i]));
        	else 
        		series.setBarColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            allSeries[i] = series;
		}
        
        setInput(dataList, showErrorBar);
        addMouseOver(dataList);
    }
	
	private void setInput (List<ChartData> dataList) {
		String[] xAxisValues = new String[dataList.size()];
        int i=0;
        if (dataList != null) {
	        for (ChartData data : dataList) {
	        	if (data.getGeneSymbol() != null)
	        		xAxisValues[i++] = data.getGeneSymbol();
	        	else
	        		xAxisValues[i++] = data.getGeneIdentifier();
			}
        }
        xAxis.setCategorySeries(xAxisValues);
        
        double[] yAxisValues = new double[dataList.size()];
        i=0;
        if (dataList != null) {
	        for (ChartData data : dataList) {
	        	if (data != null)
	        		yAxisValues[i++] = data.getValue();
			}
        }
        series.setYSeries(yAxisValues);
        IAxis yAxis = chart.getAxisSet().getYAxis(0);
        yAxis.enableLogScale(true);
        
        IErrorBar yErrorBar = series.getYErrorBar();
        yErrorBar.setType(ErrorBarType.PLUS);
        double[] errorValues = new double[dataList.size()];
        i=0;
        if (dataList != null) {
	        for (ChartData data : dataList) {
	        	if (data != null)
	        		errorValues[i++] = data.getError();
			}
        }
        yErrorBar.setPlusErrors(errorValues);
        yErrorBar.setVisible(true);
        yErrorBar.setLineWidth(2);
        chart.getAxisSet().adjustRange();
        chart.redraw();
	}
	
	private void setInput (ChartData[][] dataList, boolean showErrorBar) {
		int i=0;
		String[] xAxisValues = new String[dataList.length];
		for (ChartData[] chartDataList : dataList) {
			if (chartDataList[0] != null && chartDataList[0].getGeneSymbol() != null)
        		xAxisValues[i++] = chartDataList[0].getGeneSymbol();
        	else if (chartDataList[0] != null)
        		xAxisValues[i++] = chartDataList[0].getGeneIdentifier();
	    }
	    xAxis.setCategorySeries(xAxisValues);
	    IAxis yAxis = chart.getAxisSet().getYAxis(0);
        yAxis.enableLogScale(true);
		
	    double[][] yAxisValues=new double[dataList[0].length][dataList.length];
	    double[][] errorValues = new double[dataList[0].length][dataList.length];
	    i=0;
	    for (ChartData[] chartDataList : dataList) {
	    	if (chartDataList != null) {
		    	int j=0;
		    	for (ChartData data : chartDataList) {
		    		if (data != null) {
						yAxisValues[j][i] = data.getValue();
						errorValues[j][i] = data.getError();
						j++;
		    		}
				}
		    	i++;
	    	}
	    }
	    for (int j = 0; j < allSeries.length; j++) {
			allSeries[j].setYSeries(yAxisValues[j]);
			if (showErrorBar) {
				IErrorBar yErrorBar = allSeries[j].getYErrorBar();
		        yErrorBar.setType(ErrorBarType.PLUS);  
		        yErrorBar.setPlusErrors(errorValues[j]);
		        yErrorBar.setVisible(true);
		        yErrorBar.setLineWidth(2);
			}
		}
	    
	    chart.getAxisSet().adjustRange();
	    chart.redraw();
	    
	}
	
	public void addMouseOver (ChartData[][] dataList) {
		/* Get the plot area and add the mouse listeners */
        final Composite plotArea = chart.getPlotArea();
        
        // add mouse move listener to open tooltip on data point
        plotArea.addMouseMoveListener(new MouseMoveListener() {
		
            public void mouseMove(MouseEvent e) {
                for (ISeries series : chart.getSeriesSet().getSeries()) {
                    Rectangle[] rs = ((IBarSeries) series).getBounds();
                    for (int i = 0; i < rs.length; i++) {
                        if (rs[i] != null) {
                            if (rs[i].x < e.x && e.x < rs[i].x + rs[i].width
                                    && rs[i].y < e.y
                                    && e.y < rs[i].y + rs[i].height) {
                            	double xValue = xAxis.getDataCoordinate(rs[i].x);
                                setToolTipText(series, i, xValue);
                                return;
                            }
                        }
                    }
                }
                chart.getPlotArea().setToolTipText(null);
            }

            private void setToolTipText(ISeries series, int index, double xValue) {
                chart.getPlotArea().setToolTipText(
                        "Series: " + series.getId() + "\nValue: "
                                + series.getYSeries()[index]);
            }
        });

	}
}
