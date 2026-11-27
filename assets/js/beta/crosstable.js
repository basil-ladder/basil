(()=>{function e(e,t,n,s){Object.defineProperty(e,t,{get:n,set:s,enumerable:!0,configurable:!0})}var t=globalThis,n={},s={},i=t.parcelRequire9829;null==i&&((i=function(e){if(e in n)return n[e].exports;if(e in s){var t=s[e];delete s[e];var i={id:e,exports:{}};return n[e]=i,t.call(i.exports,i,i.exports),i.exports}var r=Error("Cannot find module '"+e+"'");throw r.code="MODULE_NOT_FOUND",r}).register=function(e,t){s[e]=t},t.parcelRequire9829=i);var r=i.register;r("aPsVO",function(t,n){e(t.exports,"html",()=>a),e(t.exports,"render",()=>i("cFPH9").render);/**
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
 */var s=i("iUyab"),r=i("eDnSP");i("iYB8H"),i("ixdbG"),i("gIbhF"),i("3jJxF"),i("cFPH9"),i("jZRBD"),i("lUGvn"),i("8AGqo"),"undefined"!=typeof window&&(window.litHtmlVersions||(window.litHtmlVersions=[])).push("1.4.1");let a=(e,...t)=>new r.TemplateResult(e,t,"html",s.defaultTemplateProcessor)}),r("iUyab",function(t,n){e(t.exports,"defaultTemplateProcessor",()=>r);/**
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
 */var s=i("3jJxF");let r=new class{handleAttributeExpressions(e,t,n,i){let r=t[0];return"."===r?new s.PropertyCommitter(e,t.slice(1),n).parts:"@"===r?[new s.EventPart(e,t.slice(1),i.eventContext)]:"?"===r?[new s.BooleanAttributePart(e,t.slice(1),n)]:new s.AttributeCommitter(e,t,n).parts}handleTextExpression(e){return new s.NodePart(e)}}}),r("3jJxF",function(t,n){e(t.exports,"AttributeCommitter",()=>u),e(t.exports,"NodePart",()=>m),e(t.exports,"BooleanAttributePart",()=>g),e(t.exports,"PropertyCommitter",()=>f),e(t.exports,"EventPart",()=>_);/**
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
 */var s=i("iYB8H"),r=i("ixdbG"),a=i("gIbhF"),o=i("lUGvn"),l=i("eDnSP"),d=i("8AGqo");let c=e=>null===e||!("object"==typeof e||"function"==typeof e),h=e=>Array.isArray(e)||!!(e&&e[Symbol.iterator]);class u{constructor(e,t,n){this.dirty=!0,this.element=e,this.name=t,this.strings=n,this.parts=[];for(let e=0;e<n.length-1;e++)this.parts[e]=this._createPart()}_createPart(){return new p(this)}_getValue(){let e=this.strings,t=e.length-1,n=this.parts;if(1===t&&""===e[0]&&""===e[1]){let e=n[0].value;if("symbol"==typeof e)return String(e);if("string"==typeof e||!h(e))return e}let s="";for(let i=0;i<t;i++){s+=e[i];let t=n[i];if(void 0!==t){let e=t.value;if(c(e)||!h(e))s+="string"==typeof e?e:String(e);else for(let t of e)s+="string"==typeof t?t:String(t)}}return s+e[t]}commit(){this.dirty&&(this.dirty=!1,this.element.setAttribute(this.name,this._getValue()))}}class p{constructor(e){this.value=void 0,this.committer=e}setValue(e){e===a.noChange||c(e)&&e===this.value||(this.value=e,(0,s.isDirective)(e)||(this.committer.dirty=!0))}commit(){for(;(0,s.isDirective)(this.value);){let e=this.value;this.value=a.noChange,e(this)}this.value!==a.noChange&&this.committer.commit()}}class m{constructor(e){this.value=void 0,this.__pendingValue=void 0,this.options=e}appendInto(e){this.startNode=e.appendChild((0,d.createMarker)()),this.endNode=e.appendChild((0,d.createMarker)())}insertAfterNode(e){this.startNode=e,this.endNode=e.nextSibling}appendIntoPart(e){e.__insert(this.startNode=(0,d.createMarker)()),e.__insert(this.endNode=(0,d.createMarker)())}insertAfterPart(e){e.__insert(this.startNode=(0,d.createMarker)()),this.endNode=e.endNode,e.endNode=this.startNode}setValue(e){this.__pendingValue=e}commit(){if(null===this.startNode.parentNode)return;for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}let e=this.__pendingValue;e!==a.noChange&&(c(e)?e!==this.value&&this.__commitText(e):e instanceof l.TemplateResult?this.__commitTemplateResult(e):e instanceof Node?this.__commitNode(e):h(e)?this.__commitIterable(e):e===a.nothing?(this.value=a.nothing,this.clear()):this.__commitText(e))}__insert(e){this.endNode.parentNode.insertBefore(e,this.endNode)}__commitNode(e){this.value!==e&&(this.clear(),this.__insert(e),this.value=e)}__commitText(e){let t=this.startNode.nextSibling,n="string"==typeof(e=null==e?"":e)?e:String(e);t===this.endNode.previousSibling&&3===t.nodeType?t.data=n:this.__commitNode(document.createTextNode(n)),this.value=e}__commitTemplateResult(e){let t=this.options.templateFactory(e);if(this.value instanceof o.TemplateInstance&&this.value.template===t)this.value.update(e.values);else{let n=new o.TemplateInstance(t,e.processor,this.options),s=n._clone();n.update(e.values),this.__commitNode(s),this.value=n}}__commitIterable(e){let t;Array.isArray(this.value)||(this.value=[],this.clear());let n=this.value,s=0;for(let i of e)void 0===(t=n[s])&&(t=new m(this.options),n.push(t),0===s?t.appendIntoPart(this):t.insertAfterPart(n[s-1])),t.setValue(i),t.commit(),s++;s<n.length&&(n.length=s,this.clear(t&&t.endNode))}clear(e=this.startNode){(0,r.removeNodes)(this.startNode.parentNode,e.nextSibling,this.endNode)}}class g{constructor(e,t,n){if(this.value=void 0,this.__pendingValue=void 0,2!==n.length||""!==n[0]||""!==n[1])throw Error("Boolean attributes can only contain a single expression");this.element=e,this.name=t,this.strings=n}setValue(e){this.__pendingValue=e}commit(){for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}if(this.__pendingValue===a.noChange)return;let e=!!this.__pendingValue;this.value!==e&&(e?this.element.setAttribute(this.name,""):this.element.removeAttribute(this.name),this.value=e),this.__pendingValue=a.noChange}}class f extends u{constructor(e,t,n){super(e,t,n),this.single=2===n.length&&""===n[0]&&""===n[1]}_createPart(){return new v(this)}_getValue(){return this.single?this.parts[0].value:super._getValue()}commit(){this.dirty&&(this.dirty=!1,this.element[this.name]=this._getValue())}}class v extends p{}let x=!1;(()=>{try{let e={get capture(){return x=!0,!1}};window.addEventListener("test",e,e),window.removeEventListener("test",e,e)}catch(e){}})();class _{constructor(e,t,n){this.value=void 0,this.__pendingValue=void 0,this.element=e,this.eventName=t,this.eventContext=n,this.__boundHandleEvent=e=>this.handleEvent(e)}setValue(e){this.__pendingValue=e}commit(){for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=a.noChange,e(this)}if(this.__pendingValue===a.noChange)return;let e=this.__pendingValue,t=this.value,n=null==e||null!=t&&(e.capture!==t.capture||e.once!==t.once||e.passive!==t.passive),i=null!=e&&(null==t||n);n&&this.element.removeEventListener(this.eventName,this.__boundHandleEvent,this.__options),i&&(this.__options=b(e),this.element.addEventListener(this.eventName,this.__boundHandleEvent,this.__options)),this.value=e,this.__pendingValue=a.noChange}handleEvent(e){"function"==typeof this.value?this.value.call(this.eventContext||this.element,e):this.value.handleEvent(e)}}let b=e=>e&&(x?{capture:e.capture,passive:e.passive,once:e.once}:e.capture)}),r("iYB8H",function(t,n){e(t.exports,"isDirective",()=>i);/**
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
 */let s=new WeakMap,i=e=>"function"==typeof e&&s.has(e)}),r("ixdbG",function(t,n){e(t.exports,"isCEPolyfill",()=>s),e(t.exports,"reparentNodes",()=>i),e(t.exports,"removeNodes",()=>r);/**
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
 */let s="undefined"!=typeof window&&null!=window.customElements&&void 0!==window.customElements.polyfillWrapFlushCallback,i=(e,t,n=null,s=null)=>{for(;t!==n;){let n=t.nextSibling;e.insertBefore(t,s),t=n}},r=(e,t,n=null)=>{for(;t!==n;){let n=t.nextSibling;e.removeChild(t),t=n}}}),r("gIbhF",function(t,n){e(t.exports,"noChange",()=>s),e(t.exports,"nothing",()=>i);/**
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
 */let s={},i={}}),r("lUGvn",function(t,n){e(t.exports,"TemplateInstance",()=>a);/**
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
 */var s=i("ixdbG"),r=i("8AGqo");class a{constructor(e,t,n){this.__parts=[],this.template=e,this.processor=t,this.options=n}update(e){let t=0;for(let n of this.__parts)void 0!==n&&n.setValue(e[t]),t++;for(let e of this.__parts)void 0!==e&&e.commit()}_clone(){let e;let t=s.isCEPolyfill?this.template.element.content.cloneNode(!0):document.importNode(this.template.element.content,!0),n=[],i=this.template.parts,a=document.createTreeWalker(t,133,null,!1),o=0,l=0,d=a.nextNode();for(;o<i.length;){if(e=i[o],!(0,r.isTemplatePartActive)(e)){this.__parts.push(void 0),o++;continue}for(;l<e.index;)l++,"TEMPLATE"===d.nodeName&&(n.push(d),a.currentNode=d.content),null===(d=a.nextNode())&&(a.currentNode=n.pop(),d=a.nextNode());if("node"===e.type){let e=this.processor.handleTextExpression(this.options);e.insertAfterNode(d.previousSibling),this.__parts.push(e)}else this.__parts.push(...this.processor.handleAttributeExpressions(d,e.name,e.strings,this.options));o++}return s.isCEPolyfill&&(document.adoptNode(t),customElements.upgrade(t)),t}}}),r("8AGqo",function(t,n){e(t.exports,"marker",()=>s),e(t.exports,"nodeMarker",()=>i),e(t.exports,"boundAttributeSuffix",()=>a),e(t.exports,"Template",()=>o),e(t.exports,"lastAttributeNameRegex",()=>h),e(t.exports,"createMarker",()=>c),e(t.exports,"isTemplatePartActive",()=>d);/**
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
 */let s=`{{lit-${String(Math.random()).slice(2)}}}`,i=`<!--${s}-->`,r=RegExp(`${s}|${i}`),a="$lit$";class o{constructor(e,t){this.parts=[],this.element=t;let n=[],i=[],o=document.createTreeWalker(t.content,133,null,!1),d=0,u=-1,p=0,{strings:m,values:{length:g}}=e;for(;p<g;){let e=o.nextNode();if(null===e){o.currentNode=i.pop();continue}if(u++,1===e.nodeType){if(e.hasAttributes()){let t=e.attributes,{length:n}=t,s=0;for(let e=0;e<n;e++)l(t[e].name,a)&&s++;for(;s-- >0;){let t=h.exec(m[p])[2],n=t.toLowerCase()+a,s=e.getAttribute(n);e.removeAttribute(n);let i=s.split(r);this.parts.push({type:"attribute",index:u,name:t,strings:i}),p+=i.length-1}}"TEMPLATE"===e.tagName&&(i.push(e),o.currentNode=e.content)}else if(3===e.nodeType){let t=e.data;if(t.indexOf(s)>=0){let s=e.parentNode,i=t.split(r),o=i.length-1;for(let t=0;t<o;t++){let n;let r=i[t];if(""===r)n=c();else{let e=h.exec(r);null!==e&&l(e[2],a)&&(r=r.slice(0,e.index)+e[1]+e[2].slice(0,-a.length)+e[3]),n=document.createTextNode(r)}s.insertBefore(n,e),this.parts.push({type:"node",index:++u})}""===i[o]?(s.insertBefore(c(),e),n.push(e)):e.data=i[o],p+=o}}else if(8===e.nodeType){if(e.data===s){let t=e.parentNode;(null===e.previousSibling||u===d)&&(u++,t.insertBefore(c(),e)),d=u,this.parts.push({type:"node",index:u}),null===e.nextSibling?e.data="":(n.push(e),u--),p++}else{let t=-1;for(;-1!==(t=e.data.indexOf(s,t+1));)this.parts.push({type:"node",index:-1}),p++}}}for(let e of n)e.parentNode.removeChild(e)}}let l=(e,t)=>{let n=e.length-t.length;return n>=0&&e.slice(n)===t},d=e=>-1!==e.index,c=()=>document.createComment(""),h=/([ \x09\x0a\x0c\x0d])([^\0-\x1F\x7F-\x9F "'>=/]+)([ \x09\x0a\x0c\x0d]*=[ \x09\x0a\x0c\x0d]*(?:[^ \x09\x0a\x0c\x0d"'`<>=]*|"[^"]*|'[^']*))$/}),r("eDnSP",function(t,n){e(t.exports,"TemplateResult",()=>l),e(t.exports,"SVGTemplateResult",()=>d);/**
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
 */var s=i("ixdbG"),r=i("8AGqo");let a=window.trustedTypes&&trustedTypes.createPolicy("lit-html",{createHTML:e=>e}),o=` ${r.marker} `;class l{constructor(e,t,n,s){this.strings=e,this.values=t,this.type=n,this.processor=s}getHTML(){let e=this.strings.length-1,t="",n=!1;for(let s=0;s<e;s++){let e=this.strings[s],i=e.lastIndexOf("<!--");n=(i>-1||n)&&-1===e.indexOf("-->",i+1);let a=(0,r.lastAttributeNameRegex).exec(e);null===a?t+=e+(n?o:r.nodeMarker):t+=e.substr(0,a.index)+a[1]+a[2]+r.boundAttributeSuffix+a[3]+r.marker}return t+this.strings[e]}getTemplateElement(){let e=document.createElement("template"),t=this.getHTML();return void 0!==a&&(t=a.createHTML(t)),e.innerHTML=t,e}}class d extends l{getHTML(){return`<svg>${super.getHTML()}</svg>`}getTemplateElement(){let e=super.getTemplateElement(),t=e.content,n=t.firstChild;return t.removeChild(n),(0,s.reparentNodes)(t,n.firstChild),e}}}),r("cFPH9",function(t,n){e(t.exports,"render",()=>l);/**
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
 */var s=i("ixdbG"),r=i("3jJxF"),a=i("jZRBD");let o=new WeakMap,l=(e,t,n)=>{let i=o.get(t);void 0===i&&((0,s.removeNodes)(t,t.firstChild),o.set(t,i=new r.NodePart(Object.assign({templateFactory:a.templateFactory},n))),i.appendInto(t)),i.setValue(e),i.commit()}}),r("jZRBD",function(t,n){e(t.exports,"templateFactory",()=>r);/**
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
 */var s=i("8AGqo");function r(e){let t=a.get(e.type);void 0===t&&(t={stringsArray:new WeakMap,keyString:new Map},a.set(e.type,t));let n=t.stringsArray.get(e.strings);if(void 0!==n)return n;let i=e.strings.join(s.marker);return void 0===(n=t.keyString.get(i))&&(n=new s.Template(e,e.getTemplateElement()),t.keyString.set(i,n)),t.stringsArray.set(e.strings,n),n}let a=new Map}),r("45wDp",function(t,n){e(t.exports,"dataBaseUrl",()=>r),e(t.exports,"racecol",()=>a),e(t.exports,"racename",()=>o),e(t.exports,"rankcol",()=>l),e(t.exports,"icon",()=>d),e(t.exports,"rankValue",()=>h),e(t.exports,"sortByRank",()=>u);var s=i("aPsVO");let r="https://data.basil-ladder.net/";function a(e){switch(e){case"PROTOSS":case"P":return"race_protoss";case"ZERG":case"Z":return"race_zerg";case"TERRAN":case"T":return"race_terran";case"RANDOM":case"R":return"race_random";default:return"race_unknown"}}function o(e){switch(e){case"PROTOSS":case"P":return"Protoss";case"ZERG":case"Z":return"Zerg";case"TERRAN":case"T":return"Terran";case"RANDOM":case"R":return"Random";default:return"Unknown"}}function l(e){return"rank_"+e.toLowerCase()}let d=(e,t)=>(0,s.html)`<svg class="icon ${t||""}" aria-hidden="true"><use href="/assets/icons.svg#${e}"></use></svg>`,c={S:0,A:1,B:2,C:3,D:4,E:5,F:6};function h(e){return e in c?c[e]:99999}function u(e){e.sort((e,t)=>{if(e.enabled!==t.enabled)return e.enabled?-1:1;let n=e.won+e.lost,s=t.won+t.lost;return n<30&&s>=30?1:s<30&&n>=30?-1:h(e.rank)!==h(t.rank)?h(e.rank)-h(t.rank):e.rating!==t.rating?t.rating-e.rating:e.win7!==t.win7?(t.win7??-1)-(e.win7??-1):t.won-e.won})}});var a=i("aPsVO"),o=i("cFPH9"),l=i("45wDp");let d=document.getElementById("crosstableControls"),c=document.getElementById("crosstable"),h=[[7,"7 days"],[30,"30 days"],[90,"90 days"],[180,"180 days"],[null,"All"]],u={range:null,enabledOnly:!0,minElo:"",maxElo:""},p=new Map,m=e=>String(e).replace(/[&<>"]/g,e=>"&#"+e.charCodeAt(0)+";");function g(e,t){let n="dark"===document.documentElement.getAttribute("data-theme"),s='<table class="crosstable-grid" style="--cols: '+t.length+'"><thead><tr><th class="corner">Bot</th><th class="elo">ELO</th>';for(let e of t)s+='<th class="colhead '+(0,l.racecol)(e.race)+'"><div>'+m(e.name)+"</div></th>";return s+="</tr></thead><tbody>",t.forEach((i,r)=>{for(let a of(s+='<tr><th class="rowhead '+(0,l.racecol)(i.race)+'"><div><span class="pos">'+(r+1)+'</span><a href="/beta/bot.html?bot='+encodeURIComponent(i.name)+'" title="'+m(i.name)+'">'+m(i.name)+'</a></div></th><th class="elo">'+(i.rating||"")+"</th>",t)){let t=e.botinfos[i.i].vsBotIdxWon[a.i],r=e.botinfos[a.i].vsBotIdxWon[i.i];i===a?s+='<td class="self"></td>':t+r===0?s+="<td></td>":s+='<td style="background:'+function(e,t,n){let s;let i=e+1,r=t+1,a=Math.min(1,r/i*(r/i)),o=Math.min(1,i/r*(i/r));if(n){let[e,t]=i>=r?[[51,133,255],a]:[[255,153,51],o];s=e.map((e,n)=>e*(1-t)+[160,160,170][n]*t)}else s=[255*a,255*o,255*Math.min(o,a)].map(Math.ceil);let l=n?[26,26,46]:[255,255,255];return"rgb("+s.map((e,t)=>Math.round(.8*e+.2*l[t])).join(",")+")"}(t,r,n)+'">'+t+"–"+r+"</td>"}s+="</tr>"}),s+"</tbody></table>"}let f=null;function v(){let e=u.range;(p.has(e)||p.set(e,fetch(l.dataBaseUrl+"stats/botVsBot"+(e?"_"+e:"")+".json").then(e=>{if(!e.ok)throw Error(e.status);return e.json()})),p.get(e)).then(t=>{if(u.range!==e)return;let n=function(e){let t=Number(u.minElo)||0,n=Number(u.maxElo)||1/0;return e.botinfos.map((e,t)=>({name:e.name,race:e.race,rating:e.rating,enabled:e.enabled,i:t})).filter(e=>(!u.enabledOnly||e.enabled)&&(!e.rating||e.rating>=t&&e.rating<=n)).sort((e,t)=>t.enabled-e.enabled||t.rating-e.rating)}(t);f={data:t,bots:n},c.innerHTML=n.length?g(t,n):'<p class="no-games">No bot matches.</p>',w()}).catch(e=>{console.error(e),c.innerHTML='<p class="no-games">Could not load the crosstable. Please try again later.</p>'})}function x(e){Object.assign(u,e),_(),v()}function _(){(0,o.render)((0,a.html)`
<div class="crosstable-controls">
    <span class="presets">${h.map(([e,t])=>(0,a.html)`
        <button class=${u.range===e?"active":""} @click=${()=>x({range:e})}>${t}</button>`)}
    </span>
    <label><input type="checkbox" .checked=${u.enabledOnly} @change=${e=>x({enabledOnly:e.target.checked})}> Enabled only</label>
    <label>ELO <input type="number" inputmode="numeric" placeholder="min" .value=${u.minElo}
        @change=${e=>x({minElo:e.target.value})}></label>
    <label>– <input type="number" inputmode="numeric" placeholder="max" .value=${u.maxElo}
        @change=${e=>x({maxElo:e.target.value})}></label>
</div>`,d)}let b=[];c.addEventListener("mouseover",e=>{let t=e.target.closest("td");if(b.forEach(e=>e.classList.remove("hl")),b=[],!t||!f)return;let n=t.parentElement,s=t.cellIndex,i=f.bots[n.sectionRowIndex],r=f.bots[s-2];(b=[n.cells[0],c.querySelector("thead tr").cells[s]]).forEach(e=>e.classList.add("hl")),t.title||i===r||(t.title=i.name+" vs "+r.name+(t.textContent?": "+t.textContent:": no games"))}),c.addEventListener("mouseleave",()=>{b.forEach(e=>e.classList.remove("hl")),b=[]});let y=window.matchMedia("(pointer: fine)");function w(){c.style.marginLeft="",c.style.width="";let e=c.getBoundingClientRect();c.style.marginLeft=-e.left+"px",c.style.width=document.documentElement.clientWidth+"px",c.style.maxHeight=y.matches?Math.max(320,window.innerHeight-(e.top+window.scrollY))+"px":""}window.addEventListener("resize",w),w(),window.basilUpdateChartsTheme=()=>{f&&(c.innerHTML=g(f.data,f.bots))},_(),v()})();
//# sourceMappingURL=crosstable.js.map
