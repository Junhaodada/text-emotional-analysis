import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle } from '../common/common'

export function baseTreemapOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
    // tooltip
    if (customAttr.tooltip) {
      const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
      const reg = new RegExp('\n', 'g')
      tooltip.formatter = tooltip.formatter.replace(reg, '<br/>')
      chart_option.tooltip = tooltip
    }
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    if (chart.data.series.length > 0) {
      // chart_option.series[0].name = chart.data.series[0].name
      // size
      if (customAttr.size) {
        chart_option.series[0].width = (customAttr.size.treemapWidth ? customAttr.size.treemapWidth : 80) + '%'
        chart_option.series[0].height = (customAttr.size.treemapHeight ? customAttr.size.treemapHeight : 80) + '%'
      }
      // label
      if (customAttr.label) {
        // chart_option.series[0].label = customAttr.label
        const l = {
          show: true,
          position: customAttr.label.position,
          color: customAttr.label.color,
          fontSize: customAttr.label.fontSize,
          formatter: customAttr.label.show ? customAttr.label.formatter : ''
        }
        chart_option.series[0].label = l
      }
      const valueArr = chart.data.series[0].data
      for (let i = 0; i < valueArr.length; i++) {
        // const y = {
        //   name: chart.data.x[i],
        //   value: valueArr[i]
        // }
        const y = valueArr[i]
        y.name = chart.data.x[i]
        // color
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % 9], customAttr.color.alpha)
        }
        // y.type = 'treemap'
        chart_option.series[0].data.push(y)
      }
      chart_option.series[0].name = chart.data.series[0].name
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
  return chart_option
}
