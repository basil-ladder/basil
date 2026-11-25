(()=>{function e(e,t,s,n){Object.defineProperty(e,t,{get:s,set:n,enumerable:!0,configurable:!0})}var t=globalThis,s={},n={},r=t.parcelRequire9829;null==r&&((r=function(e){if(e in s)return s[e].exports;if(e in n){var t=n[e];delete n[e];var r={id:e,exports:{}};return s[e]=r,t.call(r.exports,r,r.exports),r.exports}var i=Error("Cannot find module '"+e+"'");throw i.code="MODULE_NOT_FOUND",i}).register=function(e,t){n[e]=t},t.parcelRequire9829=r);var i=r.register;i("aPsVO",function(t,s){e(t.exports,"html",()=>a),e(t.exports,"render",()=>r("cFPH9").render);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("iUyab"),i=r("eDnSP");r("iYB8H"),r("ixdbG"),r("gIbhF"),r("3jJxF"),r("cFPH9"),r("jZRBD"),r("lUGvn"),r("8AGqo"),"undefined"!=typeof window&&(window.litHtmlVersions||(window.litHtmlVersions=[])).push("1.4.1");let a=(e,...t)=>new i.TemplateResult(e,t,"html",n.defaultTemplateProcessor)}),i("iUyab",function(t,s){e(t.exports,"defaultTemplateProcessor",()=>i);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("3jJxF");let i=new class{handleAttributeExpressions(e,t,s,r){let i=t[0];return"."===i?new n.PropertyCommitter(e,t.slice(1),s).parts:"@"===i?[new n.EventPart(e,t.slice(1),r.eventContext)]:"?"===i?[new n.BooleanAttributePart(e,t.slice(1),s)]:new n.AttributeCommitter(e,t,s).parts}handleTextExpression(e){return new n.NodePart(e)}}}),i("3jJxF",function(t,s){e(t.exports,"AttributeCommitter",()=>p),e(t.exports,"NodePart",()=>m),e(t.exports,"BooleanAttributePart",()=>g),e(t.exports,"PropertyCommitter",()=>f),e(t.exports,"EventPart",()=>_);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("iYB8H"),i=r("ixdbG"),a=r("gIbhF"),o=r("lUGvn"),l=r("eDnSP"),d=r("8AGqo");let c=e=>null===e||!("object"==typeof e||"function"==typeof e),h=e=>Array.isArray(e)||!!(e&&e[Symbol.iterator]);class p{constructor(e,t,s){this.dirty=!0,this.element=e,this.name=t,this.strings=s,this.parts=[];for(let e=0;e<s.length-1;e++)this.parts[e]=this._createPart()}_createPart(){return new u(this)}_getValue(){let e=this.strings,t=e.length-1,s=this.parts;if(1===t&&""===e[0]&&""===e[1]){let e=s[0].value;if("symbol"==typeof e)return String(e);if("string"==typeof e||!h(e))return e}let n="";for(let r=0;r<t;r++){n+=e[r];let t=s[r];if(void 0!==t){let e=t.value;if(c(e)||!h(e))n+="string"==typeof e?e:String(e);else for(let t of e)n+="string"==typeof t?t:String(t)}}return n+e[t]}commit(){this.dirty&&(this.dirty=!1,this.element.setAttribute(this.name,this._getValue()))}}class u{constructor(e){this.value=void 0,this.committer=e}setValue(e){e===a.noChange||c(e)&&e===this.value||(this.value=e,(0,n.isDirective)(e)||(this.committer.dirty=!0))}commit(){for(;(0,n.isDirective)(this.value);){let e=this.value;this.value=a.noChange,e(this)}this.value!==a.noChange&&this.committer.commit()}}class m{constructor(e){this.value=void 0,this.__pendingValue=void 0,this.options=e}appendInto(e){this.startNode=e.appendChild((0,d.createMarker)()),this.endNode=e.appendChild((0,d.createMarker)())}insertAfterNode(e){this.startNode=e,this.endNode=e.nextSibling}appendIntoPart(e){e.__insert(this.startNode=(0,d.createMarker)()),e.__insert(this.endNode=(0,d.createMarker)())}insertAfterPart(e){e.__insert(this.startNode=(0,d.createMarker)()),this.endNode=e.endNode,e.endNode=this.startNode}setValue(e){this.__pendingValue=e}commit(){if(null===this.startNode.parentNode)return;for(;(0,n.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}let e=this.__pendingValue;e!==a.noChange&&(c(e)?e!==this.value&&this.__commitText(e):e instanceof l.TemplateResult?this.__commitTemplateResult(e):e instanceof Node?this.__commitNode(e):h(e)?this.__commitIterable(e):e===a.nothing?(this.value=a.nothing,this.clear()):this.__commitText(e))}__insert(e){this.endNode.parentNode.insertBefore(e,this.endNode)}__commitNode(e){this.value!==e&&(this.clear(),this.__insert(e),this.value=e)}__commitText(e){let t=this.startNode.nextSibling,s="string"==typeof(e=null==e?"":e)?e:String(e);t===this.endNode.previousSibling&&3===t.nodeType?t.data=s:this.__commitNode(document.createTextNode(s)),this.value=e}__commitTemplateResult(e){let t=this.options.templateFactory(e);if(this.value instanceof o.TemplateInstance&&this.value.template===t)this.value.update(e.values);else{let s=new o.TemplateInstance(t,e.processor,this.options),n=s._clone();s.update(e.values),this.__commitNode(n),this.value=s}}__commitIterable(e){let t;Array.isArray(this.value)||(this.value=[],this.clear());let s=this.value,n=0;for(let r of e)void 0===(t=s[n])&&(t=new m(this.options),s.push(t),0===n?t.appendIntoPart(this):t.insertAfterPart(s[n-1])),t.setValue(r),t.commit(),n++;n<s.length&&(s.length=n,this.clear(t&&t.endNode))}clear(e=this.startNode){(0,i.removeNodes)(this.startNode.parentNode,e.nextSibling,this.endNode)}}class g{constructor(e,t,s){if(this.value=void 0,this.__pendingValue=void 0,2!==s.length||""!==s[0]||""!==s[1])throw Error("Boolean attributes can only contain a single expression");this.element=e,this.name=t,this.strings=s}setValue(e){this.__pendingValue=e}commit(){for(;(0,n.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}if(this.__pendingValue===a.noChange)return;let e=!!this.__pendingValue;this.value!==e&&(e?this.element.setAttribute(this.name,""):this.element.removeAttribute(this.name),this.value=e),this.__pendingValue=a.noChange}}class f extends p{constructor(e,t,s){super(e,t,s),this.single=2===s.length&&""===s[0]&&""===s[1]}_createPart(){return new v(this)}_getValue(){return this.single?this.parts[0].value:super._getValue()}commit(){this.dirty&&(this.dirty=!1,this.element[this.name]=this._getValue())}}class v extends u{}let x=!1;(()=>{try{let e={get capture(){return x=!0,!1}};window.addEventListener("test",e,e),window.removeEventListener("test",e,e)}catch(e){}})();class _{constructor(e,t,s){this.value=void 0,this.__pendingValue=void 0,this.element=e,this.eventName=t,this.eventContext=s,this.__boundHandleEvent=e=>this.handleEvent(e)}setValue(e){this.__pendingValue=e}commit(){for(;(0,n.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}if(this.__pendingValue===a.noChange)return;let e=this.__pendingValue,t=this.value,s=null==e||null!=t&&(e.capture!==t.capture||e.once!==t.once||e.passive!==t.passive),r=null!=e&&(null==t||s);s&&this.element.removeEventListener(this.eventName,this.__boundHandleEvent,this.__options),r&&(this.__options=y(e),this.element.addEventListener(this.eventName,this.__boundHandleEvent,this.__options)),this.value=e,this.__pendingValue=a.noChange}handleEvent(e){"function"==typeof this.value?this.value.call(this.eventContext||this.element,e):this.value.handleEvent(e)}}let y=e=>e&&(x?{capture:e.capture,passive:e.passive,once:e.once}:e.capture)}),i("iYB8H",function(t,s){e(t.exports,"isDirective",()=>r);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let n=new WeakMap,r=e=>"function"==typeof e&&n.has(e)}),i("ixdbG",function(t,s){e(t.exports,"isCEPolyfill",()=>n),e(t.exports,"reparentNodes",()=>r),e(t.exports,"removeNodes",()=>i);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let n="undefined"!=typeof window&&null!=window.customElements&&void 0!==window.customElements.polyfillWrapFlushCallback,r=(e,t,s=null,n=null)=>{for(;t!==s;){let s=t.nextSibling;e.insertBefore(t,n),t=s}},i=(e,t,s=null)=>{for(;t!==s;){let s=t.nextSibling;e.removeChild(t),t=s}}}),i("gIbhF",function(t,s){e(t.exports,"noChange",()=>n),e(t.exports,"nothing",()=>r);/**
 * @license
 * Copyright (c) 2018 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let n={},r={}}),i("lUGvn",function(t,s){e(t.exports,"TemplateInstance",()=>a);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("ixdbG"),i=r("8AGqo");class a{constructor(e,t,s){this.__parts=[],this.template=e,this.processor=t,this.options=s}update(e){let t=0;for(let s of this.__parts)void 0!==s&&s.setValue(e[t]),t++;for(let e of this.__parts)void 0!==e&&e.commit()}_clone(){let e;let t=n.isCEPolyfill?this.template.element.content.cloneNode(!0):document.importNode(this.template.element.content,!0),s=[],r=this.template.parts,a=document.createTreeWalker(t,133,null,!1),o=0,l=0,d=a.nextNode();for(;o<r.length;){if(e=r[o],!(0,i.isTemplatePartActive)(e)){this.__parts.push(void 0),o++;continue}for(;l<e.index;)l++,"TEMPLATE"===d.nodeName&&(s.push(d),a.currentNode=d.content),null===(d=a.nextNode())&&(a.currentNode=s.pop(),d=a.nextNode());if("node"===e.type){let e=this.processor.handleTextExpression(this.options);e.insertAfterNode(d.previousSibling),this.__parts.push(e)}else this.__parts.push(...this.processor.handleAttributeExpressions(d,e.name,e.strings,this.options));o++}return n.isCEPolyfill&&(document.adoptNode(t),customElements.upgrade(t)),t}}}),i("8AGqo",function(t,s){e(t.exports,"marker",()=>n),e(t.exports,"nodeMarker",()=>r),e(t.exports,"boundAttributeSuffix",()=>a),e(t.exports,"Template",()=>o),e(t.exports,"lastAttributeNameRegex",()=>h),e(t.exports,"createMarker",()=>c),e(t.exports,"isTemplatePartActive",()=>d);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let n=`{{lit-${String(Math.random()).slice(2)}}}`,r=`<!--${n}-->`,i=RegExp(`${n}|${r}`),a="$lit$";class o{constructor(e,t){this.parts=[],this.element=t;let s=[],r=[],o=document.createTreeWalker(t.content,133,null,!1),d=0,p=-1,u=0,{strings:m,values:{length:g}}=e;for(;u<g;){let e=o.nextNode();if(null===e){o.currentNode=r.pop();continue}if(p++,1===e.nodeType){if(e.hasAttributes()){let t=e.attributes,{length:s}=t,n=0;for(let e=0;e<s;e++)l(t[e].name,a)&&n++;for(;n-- >0;){let t=h.exec(m[u])[2],s=t.toLowerCase()+a,n=e.getAttribute(s);e.removeAttribute(s);let r=n.split(i);this.parts.push({type:"attribute",index:p,name:t,strings:r}),u+=r.length-1}}"TEMPLATE"===e.tagName&&(r.push(e),o.currentNode=e.content)}else if(3===e.nodeType){let t=e.data;if(t.indexOf(n)>=0){let n=e.parentNode,r=t.split(i),o=r.length-1;for(let t=0;t<o;t++){let s;let i=r[t];if(""===i)s=c();else{let e=h.exec(i);null!==e&&l(e[2],a)&&(i=i.slice(0,e.index)+e[1]+e[2].slice(0,-a.length)+e[3]),s=document.createTextNode(i)}n.insertBefore(s,e),this.parts.push({type:"node",index:++p})}""===r[o]?(n.insertBefore(c(),e),s.push(e)):e.data=r[o],u+=o}}else if(8===e.nodeType){if(e.data===n){let t=e.parentNode;(null===e.previousSibling||p===d)&&(p++,t.insertBefore(c(),e)),d=p,this.parts.push({type:"node",index:p}),null===e.nextSibling?e.data="":(s.push(e),p--),u++}else{let t=-1;for(;-1!==(t=e.data.indexOf(n,t+1));)this.parts.push({type:"node",index:-1}),u++}}}for(let e of s)e.parentNode.removeChild(e)}}let l=(e,t)=>{let s=e.length-t.length;return s>=0&&e.slice(s)===t},d=e=>-1!==e.index,c=()=>document.createComment(""),h=/([ \x09\x0a\x0c\x0d])([^\0-\x1F\x7F-\x9F "'>=/]+)([ \x09\x0a\x0c\x0d]*=[ \x09\x0a\x0c\x0d]*(?:[^ \x09\x0a\x0c\x0d"'`<>=]*|"[^"]*|'[^']*))$/}),i("eDnSP",function(t,s){e(t.exports,"TemplateResult",()=>l),e(t.exports,"SVGTemplateResult",()=>d);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("ixdbG"),i=r("8AGqo");let a=window.trustedTypes&&trustedTypes.createPolicy("lit-html",{createHTML:e=>e}),o=` ${i.marker} `;class l{constructor(e,t,s,n){this.strings=e,this.values=t,this.type=s,this.processor=n}getHTML(){let e=this.strings.length-1,t="",s=!1;for(let n=0;n<e;n++){let e=this.strings[n],r=e.lastIndexOf("<!--");s=(r>-1||s)&&-1===e.indexOf("-->",r+1);let a=(0,i.lastAttributeNameRegex).exec(e);null===a?t+=e+(s?o:i.nodeMarker):t+=e.substr(0,a.index)+a[1]+a[2]+i.boundAttributeSuffix+a[3]+i.marker}return t+this.strings[e]}getTemplateElement(){let e=document.createElement("template"),t=this.getHTML();return void 0!==a&&(t=a.createHTML(t)),e.innerHTML=t,e}}class d extends l{getHTML(){return`<svg>${super.getHTML()}</svg>`}getTemplateElement(){let e=super.getTemplateElement(),t=e.content,s=t.firstChild;return t.removeChild(s),(0,n.reparentNodes)(t,s.firstChild),e}}}),i("cFPH9",function(t,s){e(t.exports,"render",()=>l);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("ixdbG"),i=r("3jJxF"),a=r("jZRBD");let o=new WeakMap,l=(e,t,s)=>{let r=o.get(t);void 0===r&&((0,n.removeNodes)(t,t.firstChild),o.set(t,r=new i.NodePart(Object.assign({templateFactory:a.templateFactory},s))),r.appendInto(t)),r.setValue(e),r.commit()}}),i("jZRBD",function(t,s){e(t.exports,"templateFactory",()=>i);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var n=r("8AGqo");function i(e){let t=a.get(e.type);void 0===t&&(t={stringsArray:new WeakMap,keyString:new Map},a.set(e.type,t));let s=t.stringsArray.get(e.strings);if(void 0!==s)return s;let r=e.strings.join(n.marker);return void 0===(s=t.keyString.get(r))&&(s=new n.Template(e,e.getTemplateElement()),t.keyString.set(r,s)),t.stringsArray.set(e.strings,s),s}let a=new Map}),i("45wDp",function(t,s){e(t.exports,"dataBaseUrl",()=>i),e(t.exports,"racecol",()=>a),e(t.exports,"racename",()=>o),e(t.exports,"rankcol",()=>l),e(t.exports,"icon",()=>d),e(t.exports,"rankValue",()=>h),e(t.exports,"sortByRank",()=>p);var n=r("aPsVO");let i="https://data.basil-ladder.net/";function a(e){switch(e){case"PROTOSS":case"P":return"race_protoss";case"ZERG":case"Z":return"race_zerg";case"TERRAN":case"T":return"race_terran";case"RANDOM":case"R":return"race_random";default:return"race_unknown"}}function o(e){switch(e){case"PROTOSS":case"P":return"Protoss";case"ZERG":case"Z":return"Zerg";case"TERRAN":case"T":return"Terran";case"RANDOM":case"R":return"Random";default:return"Unknown"}}function l(e){return"rank_"+e.toLowerCase()}let d=(e,t)=>(0,n.html)`<svg class="icon ${t||""}" aria-hidden="true"><use href="/assets/icons.svg#${e}"></use></svg>`,c={S:0,A:1,B:2,C:3,D:4,E:5,F:6};function h(e){return e in c?c[e]:99999}function p(e){e.sort((e,t)=>{if(e.enabled!==t.enabled)return e.enabled?-1:1;let s=e.won+e.lost,n=t.won+t.lost;return s<30&&n>=30?1:n<30&&s>=30?-1:h(e.rank)!==h(t.rank)?h(e.rank)-h(t.rank):e.rating!==t.rating?t.rating-e.rating:e.win7!==t.win7?(t.win7??-1)-(e.win7??-1):t.won-e.won})}});var a=r("aPsVO"),o=r("cFPH9"),l=r("45wDp");let d=l.dataBaseUrl+"stats/stats.json",c=document.getElementById("stats"),h=new Intl.NumberFormat,p=new Intl.NumberFormat(void 0,{style:"percent",minimumFractionDigits:1,maximumFractionDigits:1}),u=new Intl.DateTimeFormat(void 0,{hour:"numeric"}),m=["terran","protoss","zerg","random"],g={terran:"Terran",protoss:"Protoss",zerg:"Zerg",random:"Random"},f=(e,t,s)=>(0,a.html)`
<div class="tile"><span class="label">${e}</span><span class="value">${t}</span>${s?(0,a.html)`<span class="note">${s}</span>`:""}</div>`,v=e=>(0,a.html)`<span class="race ${(0,l.racecol)(e.toUpperCase())}"></span>`,x=(e,t,s)=>(0,a.html)`
<div class="opp">
    <span class="name">${e}</span>
    <span class="wl">${h.format(t)}–${h.format(s)}</span>
    <span class="wr-bar" title="${h.format(t)} won, ${h.format(s)} lost"><span style="width: ${100*t/(t+s)}%"></span></span>
    <span class="wr">${p.format(t/(t+s))}</span>
</div>`,_=e=>{let t=e.raceCrossTable,s={terran:e.terranBots,protoss:e.protossBots,zerg:e.zergBots,random:e.randomBots},n=m.reduce((e,t)=>e+s[t],0),r=[];m.forEach((e,t)=>m.slice(t+1).forEach(t=>r.push([e,t])));let i=m.map(e=>{let s=m.filter(t=>t!==e);return{race:e,won:s.reduce((s,n)=>s+t[e][n],0),lost:s.reduce((s,n)=>s+t[n][e],0)}}),o=new Date(e.nextUpdateTime);return(0,a.html)`
<div class="stat-tiles">
    ${f("Games played",h.format(e.gamesPlayed))}
    ${f("Bots",h.format(n))}
    ${f("Crashes",h.format(e.crashes),p.format(e.crashes/e.gamesPlayed)+" of games")}
    ${f("Average game",function(e){let t=Math.round(e);return Math.floor(t/60)+":"+String(t%60).padStart(2,"0")}(e.averageGameRealtime),"minutes, real time")}
    ${f("Bot update window",u.formatRange(o,new Date(o.getTime()+36e5)),"your time")}
</div>

<h2>Bots per race</h2>
<div class="race-split">
    ${m.map(e=>(0,a.html)`<span class=${(0,l.racecol)(e.toUpperCase())} style="flex: ${s[e]}" title="${g[e]}: ${s[e]}"></span>`)}
</div>
<div class="race-split-legend">
    ${m.map(e=>(0,a.html)`<span>${v(e)} ${g[e]} <b>${s[e]}</b> <small>(${p.format(s[e]/n)})</small></span>`)}
</div>

<div class="side-by-side">
<section>
    <h2>Race matchups</h2>
    <small>Wins and losses of the first race against the second, all games since the ladder started. The line in each bar marks 50%.</small>
    <div class="winrate-list matchups">
        ${r.map(([e,s])=>x(g[e]+" vs "+g[s],t[e][s],t[s][e]))}
    </div>
</section>
<section>
    <h2>Win rate per race</h2>
    <small>Against the other races only: mirror games (e.g. Terran vs Terran) always split 50/50.</small>
    <div class="winrate-list matchups">
        ${i.map(e=>x(g[e.race],e.won,e.lost))}
    </div>
</section>
</div>`};(0,o.render)((0,a.html)`Source: <a href=${d}>${d}</a>`,document.getElementById("srcRef")),fetch(d).then(e=>e.json()).then(e=>(0,o.render)(_(e),c)).catch(e=>{console.error(e),(0,o.render)((0,a.html)`<p>Could not load the stats. Please try again later.</p>`,c)})})();
//# sourceMappingURL=stats.js.map
