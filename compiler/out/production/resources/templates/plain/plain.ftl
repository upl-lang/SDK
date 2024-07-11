<#setting number_format=0>
<#macro array a>
<#list a as i><#if i_index &gt; 0>,</#if>${i}</#list>
</#macro>
<#macro intarray a>
<#list a as i><#if i_index &gt; 0>,</#if>${i}</#list>
</#macro>
<#macro intarray3d b>
<#list b as a>
	<@intarray a/>
</#list>
</#macro>
<#if parser?has_content>
# parser
# properties
: unicode = ${unicode?string}
: bol = ${parser.bol?string}
: backup = ${parser.backup?string}
: cases = ${parser.caseCount}
: table = ${parser.table}
<#if parser.table == "ecs" || parser.table == "compressed">
: ecs = ${parser.ecsGroupCount}
</#if>
: states = ${parser.dfa.size}
# memory usage
: full table = ${((parser.eof + 1) * parser.dfa.size)}
<#if parser.table == "ecs" || parser.table == "compressed">
: ecs table = ${(parser.eof + 1 + parser.ecsGroupCount * parser.dfa.size)}
</#if>
<#if parser.table == "compressed">
: next = ${parser.dfa.next?size}
: check = ${parser.dfa.check?size}
<#if !parser.dfa.default?has_content>
: compressed table = ${(parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size)}
<#else>
: default = ${parser.dfa.default?size}
<#if !parser.dfa.meta?has_content>
: compressed table = ${(parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size + parser.dfa.default?size)}
<#else>
: meta = ${parser.dfa.meta?size}
: compressed table = ${parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size + parser.dfa.default?size + parser.dfa.meta?size}
</#if>
</#if>
</#if>
<#if parser.table == "ecs" || parser.table == "compressed">
# ecs
<@intarray parser.dfa.ecs/>
</#if>
# dfa
<#if parser.table == "ecs" || parser.table == "full">
<@intarray3d parser.dfa.table/>
</#if>
<#if parser.table == "compressed">
# compressed correctly = ${parser.dfa.correct?string}
# base
<@intarray parser.dfa.base/>
# next
<@intarray parser.dfa.next/>
# check
<@intarray parser.dfa.check/>
<#if parser.dfa.default?has_content>
# default
<@intarray parser.dfa.default/>
</#if>
<#if parser.dfa.error>
: error = ${parser.dfa.error?string}
</#if>
<#if parser.dfa.meta?has_content>
# meta
<@intarray parser.dfa.meta/>
</#if>
</#if>
# states
<@array parser.states/>
# begins
<@intarray parser.begins/>
# accepts
<@intarray parser.accept/>
# cases
<#list parser.cases as i>
<#list i.patterns as p>
# case ${p.caseValue}
{${i.action}}
</#list>
</#list>
# end
</#if>
<#if parser?has_content>
# parser
# properties
: symbols = ${parser.symbols?size}
: max terminal = ${parser.maxTerminal}
: used terminals = ${parser.usedTerminalCount}
: non-terminals = ${parser.nonTerminalCount}
: rules = ${parser.rules?size - 1}
: shift/reduce conflicts = ${parser.shiftConflict}
: reduct/reduce conflicts = ${parser.reduceConflict}
# memory usage
: ecs table = ${(parser.ecs?size + (parser.usedTerminalCount + parser.nonTerminalCount) * parser.dfa.size)}
<#if parser.table == "compressed">
: compressed table = ${parser.ecs?size + parser.dfa.totalSize}
</#if>
# ecs
<@intarray parser.ecs/>
<#if parser.table == "ecs">
# table
<@intarray3d parser.dfa.table/>
<#else>
# compressed correctly = ${parser.dfa.correct?string}
# base add
${parser.dfa.baseAdd}
# base
<@intarray parser.dfa.base/>
# next
<@intarray parser.dfa.next/>
# check
<@intarray parser.dfa.check/>
<#if parser.dfa.default?has_content>
# default
<@intarray parser.dfa.default/>
</#if>
<#if parser.dfa.error>
: error = ${parser.dfa.error?string}
</#if>
<#if parser.dfa.meta?has_content>
# meta
<@intarray parser.dfa.meta/>
</#if>
<#if parser.dfa.gotoDefault?has_content>
# goto default
<@intarray parser.dfa.gotoDefault/>
</#if>
</#if>
# rules
<@intarray parser.rules/>
# cases
<#list parser.cases as i>
<#list i.rhs as p>
# case ${p.caseValue}
{${p.action}}
</#list>
</#list>
</#if>
