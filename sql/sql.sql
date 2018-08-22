#Count the number of clusters with more than two nodes
SELECT Count(*) 
FROM   ( 
                SELECT   c.label, 
                         Count(*) AS s 
                FROM     cluster c, 
                         cluster_edit ce 
                WHERE    c.id = ce.cluster_id 
                AND      c.dst_id IS NOT NULL 
                GROUP BY c.label) t 
WHERE  s >= 2 
#Select clusters with the largest number of nodes
SELECT   * 
FROM     ( 
                  SELECT   c.label, 
                           Count(*) AS s 
                  FROM     cluster c, 
                           cluster_edit ce, 
                           edit e 
                  WHERE    c.id = ce.cluster_id 
                  AND      ce.nodes_id = e.id 
                  AND      c.dst_id IS NOT NULL 
                  AND      e.dst_id IS NOT NULL 
                  AND      e.context IS NOT NULL 
                  GROUP BY c.label) t 
WHERE    s >= 2 
ORDER BY s DESC
#Select clusters ordered by projects
SELECT   * 
FROM     ( 
                  SELECT   c.label, 
                           Count(DISTINCT(e.project)) AS s 
                  FROM     cluster c, 
                           cluster_edit ce, 
                           edit e 
                  WHERE    c.id = ce.cluster_id 
                  AND      ce.nodes_id = e.id 
                  AND      c.dst_id IS NOT NULL 
                  AND      e.dst_id IS NOT NULL 
                  AND      e.context IS NOT NULL 
                  GROUP BY c.label) t 
WHERE    s >= 2 

#Select cluster, number of edits, number of projects, and number of developers.
SELECT   * 
FROM     ( 
                  SELECT   c.label, 
                           Count(*) AS s,
                           Count(distinct(e.project)) as pj,
                           Count(distinct(e.developer)) as dev
                  FROM     cluster c, 
                           cluster_edit ce, 
                           edit e 
                  WHERE    c.id = ce.cluster_id 
                  AND      ce.nodes_id = e.id 
                  AND      c.dst_id IS NOT NULL 
                  AND      e.dst_id IS NOT NULL 
                  AND      e.context IS NOT NULL 
                  GROUP BY c.label) t 
WHERE    s >= 2 
ORDER BY pj DESC