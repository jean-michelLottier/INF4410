############## README #################

1- Dans le fichier config spécifiez les hosts ou adresses IP des serveurs, le 
numéro de port (similaire pour tous les serveurs), la ressource q en octets 
désiré pour le premier serveur (le reste pris en charge par le programme), le 
chemin du fichier étudié, et le chemin de la sortie. 
Seul le mode "secure" est opérationnel, le mode "nosecure" est implémenté 
seulement les erreurs de compilation ne sont pas encore réparées.

exemple : 
mode=secure
inputPathFile=../textes/texte_1.txt
outputPathFile=../results/result_1.txt
hosts=server_1,server_2,server_3
port=5000
q=25000

2- Lancer le jar dans le dossier dist une fois que les serveur sont opérationnels
